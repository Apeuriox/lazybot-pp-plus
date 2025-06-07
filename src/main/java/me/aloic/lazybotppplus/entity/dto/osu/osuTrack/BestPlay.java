package me.aloic.lazybotppplus.entity.dto.osu.osuTrack;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class BestPlay implements Serializable
{
    private Integer user;
    private Integer beatmap_id;
    private Integer score;
    private Double pp;
    private Integer mods;
    private String rank;
    private String score_time;
    private String update_time;

    public String getScoreKey()
    {
        return String.format("%d-%d", user, beatmap_id);
    }
}
