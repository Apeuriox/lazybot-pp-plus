package me.aloic.lazybotppplus.service.impl;

import com.alibaba.fastjson2.TypeReference;
import jakarta.annotation.Resource;
import me.aloic.lazybotppplus.entity.dto.lazybot.ScorePerformanceDTO;
import me.aloic.lazybotppplus.entity.dto.osu.beatmap.BeatmapDTO;
import me.aloic.lazybotppplus.entity.dto.osu.beatmap.ScoreLazerDTO;
import me.aloic.lazybotppplus.entity.dto.osu.player.BeatmapUserScores;
import me.aloic.lazybotppplus.entity.mapper.*;
import me.aloic.lazybotppplus.entity.po.*;
import me.aloic.lazybotppplus.entity.vo.PPPlusPerformance;
import me.aloic.lazybotppplus.entity.vo.PlayerStats;
import me.aloic.lazybotppplus.entity.vo.ScoreUpdateVO;
import me.aloic.lazybotppplus.enums.HTTPTypeEnum;
import me.aloic.lazybotppplus.enums.OsuMode;
import me.aloic.lazybotppplus.enums.PerformanceDimension;
import me.aloic.lazybotppplus.exception.InvalidScoreException;
import me.aloic.lazybotppplus.exception.LazybotRuntimeException;
import me.aloic.lazybotppplus.exception.PlayerNotFoundException;
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

    @Resource
    private ApiRequestExecutor apiRequestExecutor;

    private static final Logger logger = LoggerFactory.getLogger(PlayerServiceImpl.class);
    private static final int INITIALIZE_THREAD_LIMIT = 10;

    @Transactional
    @Override
    public PlayerStats getPlayerStats(Long id) {
        PlayerStats playerResult=checkInitializationStatus(id);
        if (playerResult != null) return playerResult;

        logger.info("[INFO] Player {} existing, calculating...", id);
        return calcPlayerStats(id);
    }

    @Transactional
    protected List<ScorePO> initializePlayerStats(Long id) {

//        List<ScoreLazerDTO> scoreLazerDTOS = new ApiRequestStarter(
//                URLBuildUtil.buildURLOfUserBest(String.valueOf(id), 100, 0, OsuMode.Osu),
//                TokenMonitor.getToken()).executeRequestForList(HTTPTypeEnum.GET, ScoreLazerDTO.class);
        List<ScoreLazerDTO> scoreLazerDTOS = getPlayerBestPerformance(id);
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
                            logger.error("pp plus calculation failed, skipping", e);
                        }
                    }
                }, VirtualThreadExecutorHolder.VIRTUAL_EXECUTOR))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        if (!allBeatmaps.isEmpty()) beatmapMapper.insertBatchIgnoreDuplicate(allBeatmaps);
        if (!allScores.isEmpty()) scoresMapper.insertBatch(allScores);
        if (!allMods.isEmpty()) scoreModMapper.insertBatch(allMods);
        if (!allStatistics.isEmpty()) scoreStatisticsMapper.insertBatch(allStatistics);
        logger.info("Player {} initialization completed", id);
        return allScores;
    }


    @Override
    @Transactional
    public PlayerStats updatePlayerStats(Long id) {
        PlayerStats playerResult=checkInitializationStatus(id);
        if (playerResult != null) return playerResult;

        List<ScoreLazerDTO> recentScores =  apiRequestExecutor.execute(
                URLBuildUtil.buildURLOfRecentCommand(String.valueOf(id),1,50, OsuMode.Osu),
                HTTPTypeEnum.GET,
                TokenMonitor.getToken(),
                null,
                new TypeReference<List<ScoreLazerDTO>>() {}
        );
        if(recentScores==null|| recentScores.isEmpty()) {
            throw new InvalidScoreException("[UPDATE] nah bro didn't even pass a map");
        }
        doUpdatesToDatabase(id, recentScores);
        return calcPlayerStats(id);
    }

    @Transactional
    @Override
    public void deleteScore(Long id) {
        ScorePO existing = scoresMapper.selectById(id);
        if (existing == null) {
            throw new InvalidScoreException("[DELETE] score not existing");
        }
        scoreModMapper.deleteByScoreId(existing.getId());
        scoreStatisticsMapper.deleteByScoreId(existing.getId());
        scoresMapper.deleteById(existing.getId());
        logger.info("successfully deleted scoreId of: {}", id);
    }


    @Transactional
    @Override
    public void updatePlayerStatsNoResult(Long id) {
        PlayerStats playerResult=checkInitializationStatus(id);
        if (playerResult != null) {
            logger.warn("[UPDATE] Player {} not existing, skipping",id);
            throw new LazybotRuntimeException("Player not existing");
        }
        List<ScoreLazerDTO> recentScores =  apiRequestExecutor.execute(
                URLBuildUtil.buildURLOfRecentCommand(String.valueOf(id),1,50,OsuMode.Osu),
                HTTPTypeEnum.GET,
                TokenMonitor.getToken(),
                null,
                new TypeReference<List<ScoreLazerDTO>>() {}
        );

        if(recentScores==null|| recentScores.isEmpty()) {
            logger.warn("[UPDATE] Player {} do not have recently played scores, skipping",id);
            throw new LazybotRuntimeException("Player do not  have recently played scores");
        }
        doUpdatesToDatabase(id, recentScores);
        playerSummaryMapper.updateTimestamp(id, LocalDateTime.now());
        logger.info("[UPDATE] Successfully updated player {}",id);
    }

    @Transactional
    @Override
    public PlayerStats reinitPlayerStats(Long id) {
        PlayerSummaryPO player = playerSummaryMapper.selectById(id);
        if (player == null) {
            logger.info("[REINIT] Player {} not initialized, initializing from scratch", id);
            playerSummaryMapper.insert(new PlayerSummaryPO(id, LocalDateTime.now()));
            List<ScorePO> scores = initializePlayerStats(id);
            return new PlayerStats(id, calculatePerformanceFromScores(scores));
        }

        List<ScoreLazerDTO> scoreLazerDTOS = getPlayerBestPerformance(id);

        int batchSize = (int) Math.ceil(scoreLazerDTOS.size() / (double) INITIALIZE_THREAD_LIMIT);
        List<List<ScoreLazerDTO>> partitions = new ArrayList<>();
        for (int i = 0; i < scoreLazerDTOS.size(); i += batchSize) {
            partitions.add(scoreLazerDTOS.subList(i, Math.min(i + batchSize, scoreLazerDTOS.size())));
        }

        List<ScorePO> calculatedScores = Collections.synchronizedList(new ArrayList<>());
        List<ScoreStatisticsPO> calculatedStats = Collections.synchronizedList(new ArrayList<>());
        List<ScoreModPO> calculatedMods = Collections.synchronizedList(new ArrayList<>());
        List<BeatmapPO> calculatedBeatmaps = Collections.synchronizedList(new ArrayList<>());

        List<CompletableFuture<Void>> futures = partitions.stream()
                .map(partition -> CompletableFuture.runAsync(() -> {
                    for (ScoreLazerDTO lazerScore : partition) {
                        try {
                            PPPlusPerformance performance = PlusPPUtil.calcPPPlusStats(
                                    AssertDownloadUtil.beatmapPath(lazerScore.getBeatmap_id(), false).toString(),
                                    lazerScore
                            );
                            calculatedScores.add(new ScorePO(lazerScore, performance));
                            calculatedStats.add(new ScoreStatisticsPO(lazerScore.getStatistics(), lazerScore.getId()));
                            calculatedBeatmaps.add(new BeatmapPO(lazerScore.getBeatmap(), lazerScore.getBeatmapset()));
                            if (lazerScore.getMods() != null && !lazerScore.getMods().isEmpty()) {
                                List<ScoreModPO> mods = lazerScore.getMods().stream()
                                        .map(mod -> new ScoreModPO(lazerScore.getId(), mod.getAcronym()))
                                        .toList();
                                calculatedMods.addAll(mods);
                            }
                        } catch (Exception e) {
                            logger.error("[REINIT] pp+ calculation failed, skipping", e);
                        }
                    }
                }, VirtualThreadExecutorHolder.VIRTUAL_EXECUTOR))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        //map od bid to score
        Map<Long, ScorePO> calculatedByBid = calculatedScores.stream()
                .collect(Collectors.toMap(
                        ScorePO::getBeatmapId,
                        Function.identity()
                ));

        Set<Long> allBids = calculatedByBid.keySet();
        Map<Long, ScorePO> existingByBid = scoresMapper
                .selectBestScoresByPlayerAndBeatmapIds(id, allBids)
                .stream()
                .collect(Collectors.toMap(
                        ScorePO::getBeatmapId,
                        Function.identity(),
                        (a, b) -> a.getPp() >= b.getPp() ? a : b
                ));


        List<ScorePO> toInsert = new ArrayList<>();
        Set<Long> winningScoreIds = new HashSet<>();
        for (Map.Entry<Long, ScorePO> entry : calculatedByBid.entrySet()) {
            Long bid = entry.getKey();
            ScorePO newScore = entry.getValue();
            ScorePO existing = existingByBid.get(bid);
            if (existing == null || newScore.getPp() > existing.getPp()) {
                if (existing != null) {
                    scoreModMapper.deleteByScoreId(existing.getId());
                    scoreStatisticsMapper.deleteByScoreId(existing.getId());
                    scoresMapper.deleteById(existing.getId());
                    logger.info("[REINIT] Deleted score on bid {}: old pp={}, preparing for new score", bid, existing.getPp());
                }
                toInsert.add(newScore);
                winningScoreIds.add(newScore.getId());
            }
        }


        if (!toInsert.isEmpty()) {
            Set<Long> winningBids = toInsert.stream().map(ScorePO::getBeatmapId).collect(Collectors.toSet());
            List<BeatmapPO> relevantBeatmaps = calculatedBeatmaps.stream()
                    .filter(b -> winningBids.contains(b.getId()))
                    .collect(Collectors.toList());
            List<ScoreStatisticsPO> relevantStats = calculatedStats.stream()
                    .filter(s -> winningScoreIds.contains(s.getScoreId()))
                    .collect(Collectors.toList());
            List<ScoreModPO> relevantMods = calculatedMods.stream()
                    .filter(m -> winningScoreIds.contains(m.getScoreId()))
                    .collect(Collectors.toList());

            if (!relevantBeatmaps.isEmpty()) beatmapMapper.insertBatchIgnoreDuplicate(relevantBeatmaps);
            scoresMapper.insertBatch(toInsert);
            if (!relevantMods.isEmpty()) scoreModMapper.insertBatch(relevantMods);
            if (!relevantStats.isEmpty()) scoreStatisticsMapper.insertBatch(relevantStats);
        }

        // 9.更新玩家时间戳并重新计算总维度数据返回
        playerSummaryMapper.updateTimestamp(id, LocalDateTime.now());
        logger.info("[REINIT] Player {} reinit completed, {} scores updated/inserted out of {} calculated",
                id, toInsert.size(), calculatedByBid.size());
        return calcPlayerStats(id);
    }


    private void doUpdatesToDatabase(Long id, List<ScoreLazerDTO> recentScores)
    {
        logger.info("[UPDATE] initial size of score list: {}" ,recentScores.size());
        recentScores=recentScores.stream().filter(score -> score.getRanked() && score.getPp()!=null).toList();
        logger.info("[UPDATE] filtered size of score list: {}" ,recentScores.size());
        if (recentScores.isEmpty()) return;
        Set<Long> beatmapIds = recentScores.stream()
                .mapToLong(ScoreLazerDTO::getBeatmap_id)
                .boxed()
                .collect(Collectors.toSet());

        Map<Long, ScorePO> existingScores = scoresMapper
                .selectBestScoresByPlayerAndBeatmapIds(id, beatmapIds)
                .stream()
                .collect(Collectors.toMap(
                        ScorePO::getBeatmapId,
                        Function.identity(),
                        (existing, incoming) -> existing.getPp() >= incoming.getPp() ? existing : incoming
                ));

        List<ScorePO> insertList = new ArrayList<>();
        List<ScoreModPO> insertMods = new ArrayList<>();
        List<ScoreStatisticsPO> insertStats = new ArrayList<>();
        List<BeatmapPO> insertBeatmaps = new ArrayList<>();
        List<ScoreUpdateVO> recalculatedRecentScores = new ArrayList<>();

        for (ScoreLazerDTO dto : recentScores) {
            try {
                PPPlusPerformance performance = PlusPPUtil.calcPPPlusStats(
                        AssertDownloadUtil.beatmapPath(dto.getBeatmap_id(), false).toString(),
                        dto);
                recalculatedRecentScores.add(new ScoreUpdateVO(new ScorePO(dto, performance), dto));
            }
            catch (Exception e) {
                logger.error("recalculate pp+ failed on {}",dto.getId());
            }
        }
        Map<Integer, ScoreUpdateVO> recentScoresMap = recalculatedRecentScores
                .stream()
                .collect(Collectors.toMap(
                        ScoreUpdateVO::getBeatmapId,
                        Function.identity(),
                        (a, b) -> a.getPpplus() >= b.getPpplus() ? a : b
                ));


        for (Map.Entry<Integer, ScoreUpdateVO> entry : recentScoresMap.entrySet()) {
            try{
                Long beatmapId = Long.valueOf(entry.getKey());
                Long scoreId = entry.getValue().getScoreInDb().getId();
                ScorePO existing = existingScores.get(beatmapId);
                if (existing == null || entry.getValue().getScoreInDb().getPp() > existing.getPp()) {
                    if (existing != null) {
                        scoreModMapper.deleteByScoreId(existing.getId());
                        scoreStatisticsMapper.deleteByScoreId(existing.getId());
                        scoresMapper.deleteById(existing.getId());
                    }
                    insertBeatmaps.add(new BeatmapPO(entry.getValue().getOriginalScore().getBeatmap(),entry.getValue().getOriginalScore().getBeatmapset()));
                    insertList.add(entry.getValue().getScoreInDb());
                    if (entry.getValue().getOriginalScore().getMods() != null) {
                        insertMods.addAll(entry.getValue().getOriginalScore().getMods().stream()
                                .map(mod -> new ScoreModPO(scoreId, mod.getAcronym()))
                                .toList());
                    }
                    insertStats.add(new ScoreStatisticsPO(entry.getValue().getOriginalScore().getStatistics(), scoreId));
                    logger.info("[UPDATE] Updated scoresId:{} on {} to {}",scoreId,beatmapId,id);
                }
                else {
                    logger.info("[UPDATE] Player already got better score on {}, ScoreId:{}",scoreId, beatmapId);
                }
            }
            catch (Exception e) {
                logger.error("[UPDATE] pp plus calculation failed, skipping", e);
            }
        }
        if (!insertBeatmaps.isEmpty()) beatmapMapper.insertBatchIgnoreDuplicate(insertBeatmaps);
        if (!insertList.isEmpty()) scoresMapper.insertBatch(insertList);
        if (!insertMods.isEmpty()) scoreModMapper.insertBatch(insertMods);
        if (!insertStats.isEmpty()) scoreStatisticsMapper.insertBatch(insertStats);
    }

    @Transactional
    @Override
    public ScorePerformanceDTO addScore(Long id, Integer beatmapId)
    {
        PlayerSummaryPO player = playerSummaryMapper.selectById(id);
        if (player == null) {
           throw new PlayerNotFoundException("Initialize player first!");
        }

        List<ScoreLazerDTO> scores = apiRequestExecutor.execute(
                URLBuildUtil.buildURLOfBeatmapScoreAll(String.valueOf(beatmapId), String.valueOf(id), OsuMode.Osu),
                HTTPTypeEnum.GET,
                TokenMonitor.getToken(),
                null,
               BeatmapUserScores.class).getScores();

        if (scores == null || scores.isEmpty()) throw new InvalidScoreException("Failed to find scores on" + beatmapId);
        scores=scores.stream().filter(score -> score.getRanked() && score.getPp()!=null).toList();
        if (scores.isEmpty()) throw new InvalidScoreException("Failed to find rankable scores on" + beatmapId);
        logger.info("[ADDSCORE] filtered size of scoreList: {}",scores.size());
        BeatmapDTO beatmapDTO = apiRequestExecutor.execute(
                URLBuildUtil.buildURLOfBeatmap(String.valueOf(beatmapId),OsuMode.Osu),
                HTTPTypeEnum.GET,
                TokenMonitor.getToken(),
                null,
                BeatmapDTO.class);

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
                logger.warn("[ADDSCORE] Failed to recalculate pp+ stats: {}", lazerScore.getId(), e);
            }
        }
        if (bestScore == null) {
            throw new InvalidScoreException("Failed to find best score for player " + id + " on beatmap " + beatmapId);
        }
        return addScore(bestScore,bestPerformance,beatmapDTO,id,beatmapId);
    }




    private ScorePerformanceDTO addScore(ScoreLazerDTO bestScore, PPPlusPerformance bestPerformance,BeatmapDTO beatmapDTO,Long id, Integer beatmapId)
    {
        ScorePO bestScorePO = new ScorePO(bestScore, bestPerformance);
        ScoreStatisticsPO statsPO = new ScoreStatisticsPO(bestScore.getStatistics(), bestScore.getId());
        List<ScoreModPO> modPOList = bestScore.getMods() == null ? List.of() :
                bestScore.getMods().stream().map(mod -> new ScoreModPO(bestScore.getId(), mod.getAcronym())).toList();
        List<ScorePO> oldScoreList = scoresMapper.selectByPlayerIdAndBeatmapId(id, beatmapId);
        BeatmapPO beatmapPO=new BeatmapPO(beatmapDTO,beatmapDTO.getBeatmapset());
        ScorePO oldScore = oldScoreList.stream()
                .max(Comparator.comparing(ScorePO::getPp))
                .orElse(null);
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
            logger.info("[ADDSCORE] overriding score: playerId={} beatmapId={} bestScore: {}", id, beatmapId, bestScore.getId());
            return getScorePerformance(bestScore.getId());
        } else {
            logger.info("[ADDSCORE] Player already got better score, skipping");
            return getScorePerformance(oldScore.getId());
        }
    }


    @Override
    public ScorePerformanceDTO getScorePerformance(Long scoreId)
    {
        ScorePerformanceDTO dto = scoresMapper.selectScoreDetailById(scoreId);
        if (dto != null) {
            List<String> mods = scoreModMapper.selectModsByScoreId(scoreId);
            dto.setMods(mods);
        }
        return dto;
    }


    @Override
    public Boolean isThisGuyMeetsTheAutoUpdateRequirement(Long id)
    {
        LocalDateTime previous2Months = LocalDateTime.now().minusMonths(2);
        return playerSummaryMapper.selectPlayerWithTime(id, previous2Months)!=null;
    }

    @Override
    public List<ScorePerformanceDTO> bestScoresInSingleDimension(Long id, PerformanceDimension dimension, Integer limit, Integer offset)
    {
        PlayerSummaryPO player = playerSummaryMapper.selectById(id);
        if (player == null) {
            throw new PlayerNotFoundException("No such player");
        }
        logger.info("[DIMENSION] Query player {}'s score on {}", id, dimension.getDbColumn());
        List<ScorePerformanceDTO> scores = scoresMapper.selectBestScoresInSingleDimensionDistinct(id, dimension.getDbColumn(), limit, offset);
        if (scores == null || scores.isEmpty()) throw new InvalidScoreException("Failed to find" + id + "'s score on" + dimension.getDbColumn());

        List<Long> scoreIds = scores.stream().map(ScorePerformanceDTO::getScoreId).toList();

        Map<Long, List<ScoreModPO>> modsMap = scoreModMapper.selectByScoreIds(scoreIds)
                .stream().collect(Collectors.groupingBy(ScoreModPO::getScoreId));

        for (ScorePerformanceDTO detail : scores) {
            List<ScoreModPO> scoreMods = (modsMap.getOrDefault(detail.getScoreId(), Collections.emptyList()));
            if (scoreMods != null && !scoreMods.isEmpty()) {
                detail.setMods(scoreMods.stream().map(ScoreModPO::getMod).toList());
            }
        }
        logger.info("[DIMENSION] Query successful");
        return scores;
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
            logger.info("[CHECKINIT] cannot find target scores for {}, initializing...", id);
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
        performance.setPpPrecision(performance.getPpPrecision() * 10);
        logger.info("[PLAYERCALC] Calculated player {}'s stats", id);
        return new PlayerStats(id, performance);
    }

    private PPPlusPerformance calculatePerformanceFromScores(List<ScorePO> scores) {
        PPPlusPerformance perf = new PPPlusPerformance();
        perf.setPp(calcWeightedTotalPerformance(getSortedScores(scores, ScorePO::getPp)));
        perf.setPpAcc(calcWeightedTotalPerformance(getSortedScores(scores, ScorePO::getPpAccuracy)));
        perf.setPpAim(calcWeightedTotalPerformance(getSortedScores(scores, ScorePO::getPpAim)));
        perf.setPpFlowAim(calcWeightedTotalPerformance(getSortedScores(scores, ScorePO::getPpFlow)));
        perf.setPpJumpAim(calcWeightedTotalPerformance(getSortedScores(scores, ScorePO::getPpJump)));
        perf.setPpPrecision(calcWeightedTotalPerformance(getSortedScores(scores, ScorePO::getPpPrecision))*10);
        perf.setPpSpeed(calcWeightedTotalPerformance(getSortedScores(scores, ScorePO::getPpSpeed)));
        perf.setPpStamina(calcWeightedTotalPerformance(getSortedScores(scores, ScorePO::getPpStamina)));
        return perf;
    }

    private List<ScoreLazerDTO> filterUnrankedScores(List<ScoreLazerDTO> scoreList)
    {

        return null;
    }
    private List<ScoreLazerDTO> getPlayerBestPerformance(Long id)
    {
        List<ScoreLazerDTO> scoreLazerDTOS = apiRequestExecutor.execute(
                URLBuildUtil.buildURLOfUserBest(String.valueOf(id), 100, 0, OsuMode.Osu),
                HTTPTypeEnum.GET,
                TokenMonitor.getToken(),
                null,
                new TypeReference<List<ScoreLazerDTO>>() {}
        );
        if (scoreLazerDTOS == null || scoreLazerDTOS.isEmpty()) {
            throw new InvalidScoreException("Player score can not be found");
        }
        logger.info("[GET BP] Initial BP size: {}", scoreLazerDTOS.size());
        if (scoreLazerDTOS.size() < 110) {
            scoreLazerDTOS.addAll(apiRequestExecutor.execute(
                    URLBuildUtil.buildURLOfUserBest(String.valueOf(id), 200 - scoreLazerDTOS.size(), scoreLazerDTOS.size(), OsuMode.Osu),
                    HTTPTypeEnum.GET,
                    TokenMonitor.getToken(),
                    null,
                    new TypeReference<List<ScoreLazerDTO>>() {}));
        }
        logger.info("[GET BP] Final BP size: {}", scoreLazerDTOS.size());
        return scoreLazerDTOS;
    }



}
