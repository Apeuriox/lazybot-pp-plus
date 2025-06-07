package me.aloic.lazybotppplus.controller;

import jakarta.annotation.Resource;
import me.aloic.lazybotppplus.entity.WebResult;
import me.aloic.lazybotppplus.service.PlayerService;
import me.aloic.lazybotppplus.util.ResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Validated
@RequestMapping("/player")
public class PlayerController
{
    @Resource
    private PlayerService playerService;
    private static final Logger logger = LoggerFactory.getLogger(PlayerController.class);


    @GetMapping("/info")
    public WebResult getPlayerInfo(@RequestParam(value = "id", required = true) Long id)
    {
        logger.info("正在处理/player/info请求");
        return ResultUtil.success(playerService.getPlayerStats(id));
    }
    @GetMapping("/add")
    public WebResult getPlayerInfo(@RequestParam(value = "id", required = true) Long id,
                                   @RequestParam(value = "beatmapId", required = true) Integer beatmapId)
    {
        logger.info("正在处理/player/add请求");
        return ResultUtil.success(playerService.addScore(id,beatmapId));
    }
}
