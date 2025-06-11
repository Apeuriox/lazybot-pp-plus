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
        logger.info("handling /player/info");
        return ResultUtil.success(playerService.getPlayerStats(id),"Query successful");
    }
    @GetMapping("/score")
    public WebResult getMapStats(@RequestParam(value = "score", required = true) Long scoreId)
    {
        logger.info("handling /player/search");
        return ResultUtil.success(playerService.getScorePerformance(scoreId),"score details successfully queried");
    }


    @PostMapping("/update")
    public WebResult updatePlayerInfo(@RequestParam(value = "id", required = true) Long id)
    {
        logger.info("handling /player/update");
        return ResultUtil.success(playerService.updatePlayerStats(id),"updated successful");
    }


    @PostMapping("/add")
    public WebResult getPlayerInfo(@RequestParam(value = "id", required = true) Long id,
                                   @RequestParam(value = "beatmapId", required = true) Integer beatmapId)
    {
        logger.info("handling /player/add");
        return ResultUtil.success(playerService.addScore(id,beatmapId),"added successful");
    }


    @GetMapping("/dimension")
    public WebResult getPlayerDetailedSkillDimension(@RequestParam(value = "id", required = true) Long id,
                                   @RequestParam(value = "dimension", required = true) String dimension,
                                   @RequestParam(value = "limit", required = true) Integer limit,
                                   @RequestParam(value = "offset", required = true) Integer offset)
    {
        logger.info("handling /player/dimension");
        return ResultUtil.success(playerService.bestScoresInSingleDimension(id, PerformanceDimension.getDimension(dimension),limit,offset),"dimension query successful");
    }
}
