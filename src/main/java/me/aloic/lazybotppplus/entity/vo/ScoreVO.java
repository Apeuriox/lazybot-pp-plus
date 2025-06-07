package me.aloic.lazybotppplus.entity.vo;

import lombok.Data;
import me.aloic.lazybotppplus.entity.dto.osu.optional.beatmap.Mod;
import me.aloic.lazybotppplus.entity.dto.osu.optional.beatmap.ScoreStatisticsLazer;

import java.util.List;
@Data
public class ScoreVO
{
    private List<Mod> mods;
    private Long score;
    private Integer maxCombo;
    private ScoreStatisticsLazer statistics;
    private Double pp;
    private String rank;
    private String createAt;
    private String avatarUrl;
    private Boolean isLazer;
    private Boolean isPerfectCombo;
}
