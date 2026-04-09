package me.aloic.lazybotppplus.service.impl;

import jakarta.annotation.Resource;
import me.aloic.lazybotppplus.entity.mapper.*;
import me.aloic.lazybotppplus.entity.vo.MetaStatistics;
import me.aloic.lazybotppplus.service.StatisticService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StatisticServiceImpl implements StatisticService
{
    @Resource
    private ScoresMapper scoresMapper;
    @Resource
    private BeatmapMapper beatmapMapper;
    @Resource
    private PlayerSummaryMapper playerSummaryMapper;


    @Override
    public MetaStatistics getCurrentMetaCount()
    {
        int beatmapCount = beatmapMapper.selectCount();
        LocalDateTime previousDay10AM = LocalDateTime.now().minusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime previous2Months = LocalDateTime.now().minusMonths(2);
        int lastDayReallyUpdatedPlayer = playerSummaryMapper.selectPlayerCountWithTime(previousDay10AM);
        int activePlayerCount = playerSummaryMapper.selectPlayerCountWithTime(previous2Months);
        int playerCount = playerSummaryMapper.selectPlayerCount();
        int scoreCount = scoresMapper.selectCount();
        return new MetaStatistics(playerCount, lastDayReallyUpdatedPlayer, activePlayerCount, scoreCount, beatmapCount);
    }


}
