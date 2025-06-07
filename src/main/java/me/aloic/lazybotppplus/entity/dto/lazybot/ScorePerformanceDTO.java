package me.aloic.lazybotppplus.entity.dto.lazybot;

import lombok.Data;
import me.aloic.lazybotppplus.entity.po.BeatmapPO;
import me.aloic.lazybotppplus.entity.po.PlayerSummaryPO;
import me.aloic.lazybotppplus.entity.po.ScoreStatisticsPO;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScorePerformanceDTO
{
    private Long scoreId;
    private Double accuracy;
    private Double pp;
    private Double ppSpeed;
    private Double ppAim;
    private Double ppStamina;
    private Double ppJump;
    private Double ppFlow;
    private Double ppPrecision;
    private Double ppAccuracy;
    private Integer combo;
    private LocalDateTime createdAt;

    private BeatmapPO beatmap;
    private PlayerSummaryPO player;
    private List<String> mods;
    private ScoreStatisticsPO statistics;
}
