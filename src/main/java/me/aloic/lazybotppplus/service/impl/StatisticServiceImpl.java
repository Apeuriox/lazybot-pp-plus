package me.aloic.lazybotppplus.service.impl;

import jakarta.annotation.Resource;
import me.aloic.lazybotppplus.entity.mapper.*;
import me.aloic.lazybotppplus.entity.vo.MetaStatistics;
import me.aloic.lazybotppplus.service.StatisticService;
import org.springframework.stereotype.Service;

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
        int playerCount = playerSummaryMapper.selectPlayerCount();
        int scoreCount = scoresMapper.selectCount();
        return new MetaStatistics(playerCount, scoreCount, beatmapCount);
    }



}
