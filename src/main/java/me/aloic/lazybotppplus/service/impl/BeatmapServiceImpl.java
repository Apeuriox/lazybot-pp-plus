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
import me.aloic.lazybotppplus.util.URLBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class BeatmapServiceImpl implements BeatmapService
{
    private static final Logger logger = LoggerFactory.getLogger(BeatmapServiceImpl.class);
    private static final int BEATMAP_UPDATE_PAGE_SIZE = 100;

    @Resource
    private BeatmapMapper beatmapMapper;

    @Resource
    private ApiRequestExecutor apiRequestExecutor;

    @Transactional
    @Override
    public void updatedBeatmapCache()
    {
        logger.info("[MAP_UPDATE] trying to update all null sid maps...");
        long start = System.currentTimeMillis();

        int currentPage = 1;
        int totalUpdatedMapsCount = 0;
        ExecutorService executor = Executors.newFixedThreadPool(15);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        while (true) {
            List<BeatmapPO> maps = beatmapMapper.selectAllNullSidMaps(BEATMAP_UPDATE_PAGE_SIZE * ( currentPage - 1 ), BEATMAP_UPDATE_PAGE_SIZE);
            if (maps == null || maps.isEmpty()) {
                break;
            }
            for (BeatmapPO map : maps) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        BeatmapDTO beatmapDTO = apiRequestExecutor.execute(
                                URLBuildUtil.buildURLOfBeatmap(String.valueOf(map.getId()), OsuMode.Osu),
                                HTTPTypeEnum.GET,
                                TokenMonitor.getToken(),
                                null,
                                BeatmapDTO.class);
                        beatmapMapper.updateBeatmapSidById(map.getId(), beatmapDTO.getBeatmapset_id());
                    } catch (Exception e) {
                        logger.error("Failed to update beatmap {}: {}", map.getId(), e.getMessage(), e);
                    }
                }, executor);
                futures.add(future);
            }
            totalUpdatedMapsCount+=maps.size();
            currentPage++;
            if (maps.size()< BEATMAP_UPDATE_PAGE_SIZE) {
                break;
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long end = System.currentTimeMillis();
        logger.info("All beatmap updated！ updated_size: {}", totalUpdatedMapsCount);
        if (totalUpdatedMapsCount !=0)
            logger.info("Total time consumed: {}ms, Avg.: {}ms/beatmap", (end - start), (end - start) / totalUpdatedMapsCount);
    }


}
