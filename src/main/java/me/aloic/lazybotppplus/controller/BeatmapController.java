package me.aloic.lazybotppplus.controller;

import jakarta.annotation.Resource;
import me.aloic.lazybotppplus.entity.WebResult;
import me.aloic.lazybotppplus.service.BeatmapService;
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
@RequestMapping("/beatmap")
public class BeatmapController
{

    @Resource
    private BeatmapService beatmapService;

    private static final Logger logger = LoggerFactory.getLogger(BeatmapController.class);

    @GetMapping("/updateAll")
    public WebResult updateNullSidBeatmaps()
    {
        logger.info("handling /beatmap/updateAll");
        beatmapService.updatedBeatmapCache();
        return ResultUtil.success(null,"dimension query successful");
    }
}
