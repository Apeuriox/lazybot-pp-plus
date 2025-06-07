package me.aloic.lazybotppplus.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import me.aloic.lazybotppplus.entity.dto.lazybot.ScorePerformanceDTO;
import me.aloic.lazybotppplus.entity.dto.osu.beatmap.ScoreLazerDTO;
import me.aloic.lazybotppplus.entity.mapper.*;
import me.aloic.lazybotppplus.entity.po.*;
import me.aloic.lazybotppplus.entity.vo.PPPlusPerformance;
import me.aloic.lazybotppplus.entity.vo.PlayerStats;
import me.aloic.lazybotppplus.enums.HTTPTypeEnum;
import me.aloic.lazybotppplus.enums.OsuMode;
import me.aloic.lazybotppplus.enums.PerformanceDimension;
import me.aloic.lazybotppplus.exception.LazybotRuntimeException;
import me.aloic.lazybotppplus.monitor.TokenMonitor;
import me.aloic.lazybotppplus.service.PlayerService;
import me.aloic.lazybotppplus.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.IntStream;

@Service
public class PlayerServiceImpl implements PlayerService
{
    @Resource
    private ScoresMapper scoresMapper;

    @Resource
    private ScoreModMapper scoreModMapper;

    @Resource
    private ScoreStatisticsMapper scoreStatisticsMapper;

    @Resource
    private BeatmapMapper beatmapMapper;
    @Resource
    private PlayerSummaryMapper playerSummaryMapper;

    private static final Logger logger = LoggerFactory.getLogger(PlayerServiceImpl.class);
    private static final int INITIALIZE_THREAD_LIMIT = 10;

    @Transactional
    @Override
    public PlayerStats getPlayerStats(Long id) {
        PlayerSummaryPO player = playerSummaryMapper.selectById(id);
        PPPlusPerformance performance = new PPPlusPerformance();

        if (player == null) {
            logger.info("请求玩家{}无数据，正在初始化...", id);
            playerSummaryMapper.insert(new PlayerSummaryPO(id, LocalDateTime.now()));
            List<ScorePO> scores = initializePlayerStats(id);
            return new PlayerStats(id, calculatePerformanceFromScores(scores));
        }
        logger.info("请求玩家{}存在，正在计算...", id);
        Map<PerformanceDimension, List<ScorePO>> dimensionScoreMap = new EnumMap<>(PerformanceDimension.class);
        for (PerformanceDimension dim : PerformanceDimension.values()) {
            dimensionScoreMap.put(dim, scoresMapper.selectTopScoresByPlayerIdAndDimension(id, dim.getDbColumn()));
        }
        for (PerformanceDimension dim : PerformanceDimension.values()) {
            List<Double> sortedValues = getSortedScores(dimensionScoreMap.get(dim), dim.getGetter());
            double result = calcWeightedTotalPerformance(sortedValues);
            dim.getSetter().accept(performance, result);
        }
        logger.info("计算完成");
        return new PlayerStats(id, performance);
    }

    @Transactional
    protected List<ScorePO> initializePlayerStats(Long id) {
        List<ScoreLazerDTO> scoreLazerDTOS = new ApiRequestStarter(
                URLBuildUtil.buildURLOfUserBest(String.valueOf(id), 100, 0, OsuMode.Osu),
                TokenMonitor.getToken()
        ).executeRequestForList(HTTPTypeEnum.GET, ScoreLazerDTO.class);
        if (scoreLazerDTOS == null || scoreLazerDTOS.isEmpty()) {
            throw new LazybotRuntimeException("找不到此玩家的成绩");
        }
        logger.info("玩家初始请求成绩列表大小: {}", scoreLazerDTOS.size());
        if (scoreLazerDTOS.size() < 110) {
            scoreLazerDTOS.addAll(new ApiRequestStarter(
                    URLBuildUtil.buildURLOfUserBest(String.valueOf(id), 200-scoreLazerDTOS.size(), scoreLazerDTOS.size(), OsuMode.Osu),
                    TokenMonitor.getToken()).executeRequestForList(HTTPTypeEnum.GET, ScoreLazerDTO.class));
        }
        logger.info("玩家BP成绩列表最终大小: {}", scoreLazerDTOS.size());
        int batchSize = (int) Math.ceil(scoreLazerDTOS.size() / (double) INITIALIZE_THREAD_LIMIT);
        List<List<ScoreLazerDTO>> partitions = new ArrayList<>();
        for (int i = 0; i < scoreLazerDTOS.size(); i += batchSize) {
            partitions.add(scoreLazerDTOS.subList(i, Math.min(i + batchSize, scoreLazerDTOS.size())));
        }
        List<ScorePO> allScores = Collections.synchronizedList(new ArrayList<>());
        List<ScoreStatisticsPO> allStatistics = Collections.synchronizedList(new ArrayList<>());
        List<ScoreModPO> allMods = Collections.synchronizedList(new ArrayList<>());
        List<BeatmapPO> allBeatmaps = Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<Void>> futures = partitions.stream()
                .map(partition -> CompletableFuture.runAsync(() -> {
                    for (ScoreLazerDTO lazerScore : partition) {
                        try {
                            PPPlusPerformance performance = PlusPPUtil.calcPPPlusStats(
                                    AssertDownloadUtil.beatmapPath(lazerScore.getBeatmap_id(), false).toString(),
                                    lazerScore
                            );

                            allScores.add(new ScorePO(lazerScore, performance));
                            allStatistics.add(new ScoreStatisticsPO(lazerScore.getStatistics(), lazerScore.getId()));
                            allBeatmaps.add(new BeatmapPO(lazerScore.getBeatmap(),lazerScore.getBeatmapset()));

                            if (lazerScore.getMods() != null && !lazerScore.getMods().isEmpty()) {
                                List<ScoreModPO> mods = lazerScore.getMods().stream()
                                        .map(mod -> new ScoreModPO(lazerScore.getId(), mod.getAcronym()))
                                        .toList();
                                allMods.addAll(mods);
                            }
                        } catch (Exception e) {
                            logger.error("计算PP+失败, 跳过该记录", e);
                        }
                    }
                }, VirtualThreadExecutorHolder.VIRTUAL_EXECUTOR))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        if (!allBeatmaps.isEmpty()) beatmapMapper.insertBatchIgnoreDuplicate(allBeatmaps);
        if (!allScores.isEmpty()) scoresMapper.insertBatch(allScores);
        if (!allMods.isEmpty()) scoreModMapper.insertBatch(allMods);
        if (!allStatistics.isEmpty()) scoreStatisticsMapper.insertBatch(allStatistics);
        logger.info("完成玩家资料初始化");
        return allScores;
    }


    public PlayerStats updatePlayerStats(Long id) {
        return null;
    }

    public PlayerStats addScore(Long id, Integer beatmapId)
    {

        return null;
    }

    @Override
    public ScorePerformanceDTO getScorePerformance(Long id)
    {
        ScorePerformanceDTO dto = scoresMapper.selectScoreDetailById(id);
        if (dto != null) {
            List<String> mods = scoreModMapper.selectModsByScoreId(id);
            dto.setMods(mods);
        }
        return dto;
    }


    public PlayerStats typeScoreLeaderboard()
    {
        return null;
    }
    private static double calcWeightedTotalPerformance(List<Double> scores)
    {
        return IntStream.range(0, scores.size())
            .mapToDouble(i -> Math.pow(0.95, i) * scores.get(i))
            .sum();
    }
    private <T extends Number> List<Double> getSortedScores(List<ScorePO> scores, Function<ScorePO, T> getter) {
        return scores.stream()
                .map(getter)
                .filter(Objects::nonNull)
                .map(Number::doubleValue)
                .sorted(Comparator.reverseOrder())
                .toList();
    }

    private PPPlusPerformance calculatePerformanceFromScores(List<ScorePO> scores) {
        PPPlusPerformance perf = new PPPlusPerformance();
        perf.setPp(calcWeightedTotalPerformance(getSortedScores(scores, ScorePO::getPp)));
        perf.setPpAcc(calcWeightedTotalPerformance(getSortedScores(scores, ScorePO::getPpAccuracy)));
        perf.setPpAim(calcWeightedTotalPerformance(getSortedScores(scores, ScorePO::getPpAim)));
        perf.setPpFlowAim(calcWeightedTotalPerformance(getSortedScores(scores, ScorePO::getPpFlow)));
        perf.setPpJumpAim(calcWeightedTotalPerformance(getSortedScores(scores, ScorePO::getPpJump)));
        perf.setPpPrecision(calcWeightedTotalPerformance(getSortedScores(scores, ScorePO::getPpPrecision)));
        perf.setPpSpeed(calcWeightedTotalPerformance(getSortedScores(scores, ScorePO::getPpSpeed)));
        perf.setPpStamina(calcWeightedTotalPerformance(getSortedScores(scores, ScorePO::getPpStamina)));
        return perf;
    }
}
