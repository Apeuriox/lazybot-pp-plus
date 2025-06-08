package me.aloic.lazybotppplus.service.impl;

import jakarta.annotation.Resource;
import me.aloic.lazybotppplus.entity.dto.lazybot.ScorePerformanceDTO;
import me.aloic.lazybotppplus.entity.dto.osu.beatmap.BeatmapDTO;
import me.aloic.lazybotppplus.entity.dto.osu.beatmap.ScoreLazerDTO;
import me.aloic.lazybotppplus.entity.dto.osu.player.BeatmapUserScores;
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
import java.util.stream.Collectors;
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
        PlayerStats playerResult=checkInitializationStatus(id);
        if (playerResult != null) return playerResult;

        logger.info("请求玩家{}存在，正在计算...", id);
        return calcPlayerStats(id);
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


    @Override
    @Transactional
    public PlayerStats updatePlayerStats(Long id) {
        PlayerStats playerResult=checkInitializationStatus(id);
        if (playerResult != null) return playerResult;

        List<ScoreLazerDTO> recentScores =  new ApiRequestStarter(URLBuildUtil.buildURLOfRecentCommand(String.valueOf(id),1,50,OsuMode.Osu),TokenMonitor.getToken())
                .executeRequestForList(HTTPTypeEnum.GET, ScoreLazerDTO.class);
        if(recentScores==null|| recentScores.isEmpty()) {
            throw new LazybotRuntimeException("我只能说bro你没打图");
        }
        Set<Long> beatmapIds = recentScores.stream()
                .mapToLong(ScoreLazerDTO::getBeatmap_id)
                .boxed()
                .collect(Collectors.toSet());

        Map<Long, ScorePO> existingScores = scoresMapper
                .selectBestScoresByPlayerAndBeatmapIds(id, beatmapIds)
                .stream()
                .collect(Collectors.toMap(ScorePO::getBeatmapId, Function.identity()));

        List<ScorePO> insertList = new ArrayList<>();
        List<ScoreModPO> insertMods = new ArrayList<>();
        List<ScoreStatisticsPO> insertStats = new ArrayList<>();
        List<BeatmapPO> insertBeatmaps = new ArrayList<>();
        for (ScoreLazerDTO dto : recentScores) {
            try{
                Long beatmapId = Long.valueOf(dto.getBeatmap_id());
                Long scoreId = dto.getId();

                PPPlusPerformance performance = PlusPPUtil.calcPPPlusStats(AssertDownloadUtil.beatmapPath(Math.toIntExact(beatmapId), false).toString(), dto);
                ScorePO newScore = new ScorePO(dto, performance);
                ScorePO existing = existingScores.get(beatmapId);
                if (existing == null || newScore.getPp() > existing.getPp()) {
                    if (existing != null) {
                        scoresMapper.deleteById(existing.getId());
                        scoreModMapper.deleteByScoreId(existing.getId());
                        scoreStatisticsMapper.deleteByScoreId(existing.getId());
                    }
                    insertBeatmaps.add(new BeatmapPO(dto.getBeatmap(),dto.getBeatmapset()));
                    insertList.add(newScore);
                    if (dto.getMods() != null) {
                        insertMods.addAll(dto.getMods().stream()
                                .map(mod -> new ScoreModPO(scoreId, mod.getAcronym()))
                                .toList());
                    }
                    insertStats.add(new ScoreStatisticsPO(dto.getStatistics(), scoreId));
                    logger.info("已更新成绩id:{}在{}的成绩到{}",scoreId,beatmapId,id);
                }
            }
            catch (Exception e) {
                logger.error("计算PP+失败, 跳过该记录", e);
            }
        }
        if (!insertStats.isEmpty()) beatmapMapper.insertBatchIgnoreDuplicate(insertBeatmaps);
        if (!insertList.isEmpty()) scoresMapper.insertBatch(insertList);
        if (!insertMods.isEmpty()) scoreModMapper.insertBatch(insertMods);
        if (!insertStats.isEmpty()) scoreStatisticsMapper.insertBatch(insertStats);

        return calcPlayerStats(id);
    }

    @Transactional
    @Override
    public ScorePerformanceDTO addScore(Long id, Integer beatmapId)
    {
        PlayerSummaryPO player = playerSummaryMapper.selectById(id);
        if (player == null) {
           throw new LazybotRuntimeException("找不到该玩家");
        }

        List<ScoreLazerDTO> scores = new ApiRequestStarter(
                URLBuildUtil.buildURLOfBeatmapScoreAll(String.valueOf(beatmapId), String.valueOf(id), OsuMode.Osu),
                TokenMonitor.getToken())
                .executeRequest(HTTPTypeEnum.GET, BeatmapUserScores.class).getScores();

        if (scores == null || scores.isEmpty()) throw new LazybotRuntimeException("找不到该玩家在" + beatmapId + "上的成绩");
        BeatmapDTO beatmapDTO = new ApiRequestStarter(URLBuildUtil.buildURLOfBeatmap(String.valueOf(beatmapId),OsuMode.Osu), TokenMonitor.getToken())
                .executeRequest(HTTPTypeEnum.GET, BeatmapDTO.class);

        ScoreLazerDTO bestScore = null;
        PPPlusPerformance bestPerformance = null;

        for (ScoreLazerDTO lazerScore : scores) {
            try {
                PPPlusPerformance performance = PlusPPUtil.calcPPPlusStats(AssertDownloadUtil.beatmapPath(lazerScore.getBeatmap_id(), false).toString(), lazerScore);
                if (bestScore == null || performance.getPp() > bestPerformance.getPp()) {
                    bestScore = lazerScore;
                    bestPerformance = performance;
                }
            } catch (Exception e) {
                logger.warn("计算失败，跳过成绩: {}", lazerScore.getId(), e);
            }
        }
        if (bestScore == null) {
            throw new LazybotRuntimeException("无法获得有效的最佳成绩");
        }
        return addScore(bestScore,bestPerformance,beatmapDTO,id,beatmapId);
    }

    private ScorePerformanceDTO addScore(ScoreLazerDTO bestScore, PPPlusPerformance bestPerformance,BeatmapDTO beatmapDTO,Long id, Integer beatmapId)
    {
        ScorePO bestScorePO = new ScorePO(bestScore, bestPerformance);
        ScoreStatisticsPO statsPO = new ScoreStatisticsPO(bestScore.getStatistics(), bestScore.getId());
        List<ScoreModPO> modPOList = bestScore.getMods() == null ? List.of() :
                bestScore.getMods().stream().map(mod -> new ScoreModPO(bestScore.getId(), mod.getAcronym())).toList();
        ScorePO oldScore = scoresMapper.selectByPlayerIdAndBeatmapId(id, beatmapId);
        BeatmapPO beatmapPO=new BeatmapPO(beatmapDTO,beatmapDTO.getBeatmapset());
        if (oldScore == null || oldScore.getPp() < bestPerformance.getPp()) {
            if (oldScore != null) {
                scoreModMapper.deleteByScoreId(oldScore.getId());
                scoreStatisticsMapper.deleteByScoreId(oldScore.getId());
                scoresMapper.deleteById(oldScore.getId());
            }
            beatmapMapper.insertOrUpdate(beatmapPO);
            scoresMapper.insert(bestScorePO);
            scoreStatisticsMapper.insertSingle(statsPO);
            if (!modPOList.isEmpty()) {
                scoreModMapper.insertBatch(modPOList);
            }
            logger.info("替换 playerId={} beatmapId={} 的 bestScore: {}", id, beatmapId, bestScore.getId());
            return scoresMapper.selectScoreDetailById(bestScore.getId());
        } else {
            logger.info("已有更高分数，跳过更新");
            return scoresMapper.selectScoreDetailById(oldScore.getId());
        }
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
    @Transactional
    protected PlayerStats checkInitializationStatus(Long id)
    {
        PlayerSummaryPO player = playerSummaryMapper.selectById(id);
        if (player == null) {
            logger.info("请求玩家{}无数据，正在初始化...", id);
            playerSummaryMapper.insert(new PlayerSummaryPO(id, LocalDateTime.now()));
            List<ScorePO> scores = initializePlayerStats(id);
            return new PlayerStats(id, calculatePerformanceFromScores(scores));
        }
        return null;
    }
    @Transactional
    protected PlayerStats calcPlayerStats(Long id) {
        PPPlusPerformance performance = new PPPlusPerformance();
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
