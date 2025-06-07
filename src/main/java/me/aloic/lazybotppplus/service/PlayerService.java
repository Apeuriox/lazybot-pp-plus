package me.aloic.lazybotppplus.service;

import me.aloic.lazybotppplus.entity.dto.lazybot.ScorePerformanceDTO;
import me.aloic.lazybotppplus.entity.vo.PlayerStats;
import org.springframework.transaction.annotation.Transactional;

public interface PlayerService
{
    @Transactional
    PlayerStats getPlayerStats(Long id);

    ScorePerformanceDTO addScore(Long id, Integer beatmapId);

    ScorePerformanceDTO getScorePerformance(Long id);
}
