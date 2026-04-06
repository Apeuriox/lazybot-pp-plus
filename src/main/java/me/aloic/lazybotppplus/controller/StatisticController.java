package me.aloic.lazybotppplus.controller;

import jakarta.annotation.Resource;
import me.aloic.lazybotppplus.entity.WebResult;
import me.aloic.lazybotppplus.service.StatisticService;
import me.aloic.lazybotppplus.util.ResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@Validated
@RequestMapping("/stats")
public class StatisticController
{
    @Resource
    private StatisticService statisticService;
    private static final Logger logger = LoggerFactory.getLogger(StatisticController.class);

    @GetMapping("/count")
    public WebResult updateNullSidBeatmaps()
    {
        logger.info("handling /stats/count");
        return ResultUtil.success( statisticService.getCurrentMetaCount(),"dimension query successful");
    }
}
