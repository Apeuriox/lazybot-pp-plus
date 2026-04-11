package me.aloic.lazybotppplus.controller;

import jakarta.annotation.Resource;
import me.aloic.lazybotppplus.entity.WebResult;
import me.aloic.lazybotppplus.metrics.APIMetrics;
import me.aloic.lazybotppplus.monitor.PlayerUpdateMonitor;
import me.aloic.lazybotppplus.service.StatisticService;
import me.aloic.lazybotppplus.util.ResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Validated
@RequestMapping("/stats")
public class StatisticController
{
    @Resource
    private StatisticService statisticService;
    @Resource
    private APIMetrics apiMetrics;
    @Resource
    private PlayerUpdateMonitor playerUpdateMonitor;

    private static final Logger logger = LoggerFactory.getLogger(StatisticController.class);

    @GetMapping("/count")
    public WebResult updateNullSidBeatmaps()
    {
        logger.info("handling /stats/count");
        return ResultUtil.success( statisticService.getCurrentMetaCount(),"basic count query successful");
    }

    @GetMapping("/usage")
    public WebResult getApiUsageSinceStart()
    {
        logger.info("handling /stats/usage");
        return ResultUtil.success(apiMetrics.getStatsMap(),"getting target api usage successful");
    }
    @GetMapping("/player/updated")
    public WebResult getHowManyPeopleReallyUpdatedLastTime()
    {
        logger.info("handling /stats/player/updated");
        return ResultUtil.success(playerUpdateMonitor.getCountOfUpdatedPlayerLastTime().sum(),"last time player update count get successful");
    }

}
