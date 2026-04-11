package me.aloic.lazybotppplus.service.impl;

import jakarta.annotation.Resource;
import me.aloic.lazybotppplus.entity.dto.osu.beatmap.BeatmapDTO;
import me.aloic.lazybotppplus.entity.mapper.BeatmapMapper;
import me.aloic.lazybotppplus.entity.po.BeatmapPO;
import me.aloic.lazybotppplus.enums.HTTPTypeEnum;
import me.aloic.lazybotppplus.enums.OsuMode;
import me.aloic.lazybotppplus.monitor.TokenMonitor;
import me.aloic.lazybotppplus.service.BeatmapService;
import me.aloic.lazybotppplus.util.ApiRequestExecutor;
import me.aloic.lazybotppplus.util.RateLimiter;
import me.aloic.lazybotppplus.util.URLBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

@Service
public class BeatmapServiceImpl implements BeatmapService
{
    private static final Logger logger = LoggerFactory.getLogger(BeatmapServiceImpl.class);

    private static final int BEATMAP_UPDATE_PAGE_SIZE = 100;
    @Resource
    private BeatmapMapper beatmapMapper;
    @Resource
    private ApiRequestExecutor apiRequestExecutor;
    RateLimiter limiter = new RateLimiter(18.0);
    private final LongAdder updatedBeatmapCount = new LongAdder();

    @Override
    public void updatedBeatmapCache() {
        logger.info("[MAP_UPDATE] Starting update...");
        long start = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(15);
        try {
            while (true) {
                List<BeatmapPO> maps = beatmapMapper.selectAllNullSidMaps(BEATMAP_UPDATE_PAGE_SIZE);
                if (maps == null || maps.isEmpty()) break;
                List<CompletableFuture<BeatmapPO>> futures = maps.stream().map(map ->
                        CompletableFuture.supplyAsync(() -> {
                            try {
                                limiter.acquire();
                                BeatmapDTO dto = apiRequestExecutor.execute(
                                        URLBuildUtil.buildURLOfBeatmap(String.valueOf(map.getId()), OsuMode.Osu),
                                        HTTPTypeEnum.GET, TokenMonitor.getToken(), null, BeatmapDTO.class);

                                map.setSid(Long.valueOf(dto.getBeatmapset_id()));
                                return map;
                            } catch (Exception e) {
                                logger.error("[MAP_UPDATE] API query failed for id {}: {}", map.getId(), e.getMessage());
                                return null;
                            }
                        }, executor)
                ).toList();

                List<BeatmapPO> results = futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .filter(m -> m.getSid() != null)
                        .collect(Collectors.toList());

                if (!results.isEmpty()) {
                    beatmapMapper.batchUpdateButOnlySid(results);
                    updatedBeatmapCount.add(results.size());
                }

                logger.info("[MAP_UPDATE] Map update progress: total {} maps updated!", updatedBeatmapCount.sum());
            }
        } finally {
            executor.shutdown();
        }

        long end = System.currentTimeMillis();
        logger.info("[MAP_UPDATE] Update finished! Total: {}, Time consumed: {}ms", updatedBeatmapCount.sum(), (end - start));
    }



}
