package me.aloic.lazybotppplus.service;

import me.aloic.lazybotppplus.entity.dto.lazybot.ScorePerformanceDTO;
import me.aloic.lazybotppplus.entity.vo.PlayerStats;
import me.aloic.lazybotppplus.enums.PerformanceDimension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PlayerService
{
    @Transactional
    PlayerStats getPlayerStats(Long id);

    @Transactional
    PlayerStats updatePlayerStats(Long id);

    @Transactional
    void updatePlayerStatsNoResult(Long id);

    ScorePerformanceDTO addScore(Long id, Integer beatmapId);

    ScorePerformanceDTO getScorePerformance(Long id);

    List<ScorePerformanceDTO> bestScoresInSingleDimension(Long id, PerformanceDimension dimension, Integer limit, Integer offset);

}
