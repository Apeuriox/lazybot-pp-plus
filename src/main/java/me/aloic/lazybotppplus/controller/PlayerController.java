package me.aloic.lazybotppplus.controller;

import jakarta.annotation.Resource;
import me.aloic.lazybotppplus.entity.WebResult;
import me.aloic.lazybotppplus.enums.PerformanceDimension;
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
        return ResultUtil.success(playerService.getPlayerStats(id),"成功获取");
    }
    @GetMapping("/score")
    public WebResult getMapStats(@RequestParam(value = "score", required = true) Long scoreId)
    {
        logger.info("正在处理/player/search请求");
        return ResultUtil.success(playerService.getScorePerformance(scoreId),"成功获取成绩");
    }


    @PostMapping("/update")
    public WebResult updatePlayerInfo(@RequestParam(value = "id", required = true) Long id)
    {
        logger.info("正在处理/player/update请求");
        return ResultUtil.success(playerService.updatePlayerStats(id),"更新成功");
    }


    @PostMapping("/add")
    public WebResult getPlayerInfo(@RequestParam(value = "id", required = true) Long id,
                                   @RequestParam(value = "beatmapId", required = true) Integer beatmapId)
    {
        logger.info("正在处理/player/add请求");
        return ResultUtil.success(playerService.addScore(id,beatmapId),"添加成功");
    }


    @GetMapping("/dimension")
    public WebResult getPlayerDetailedSkillDimension(@RequestParam(value = "id", required = true) Long id,
                                   @RequestParam(value = "dimension", required = true) String dimension,
                                   @RequestParam(value = "limit", required = true) Integer limit,
                                   @RequestParam(value = "offset", required = true) Integer offset)
    {
        logger.info("正在处理/player/dimension请求");
        return ResultUtil.success(playerService.bestScoresInSingleDimension(id, PerformanceDimension.getDimension(dimension),limit,offset),"成功查询");
    }
}
