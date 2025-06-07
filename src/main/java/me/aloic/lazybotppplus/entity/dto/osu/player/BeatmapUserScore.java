package me.aloic.lazybotppplus.entity.dto.osu.player;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.aloic.lazybotppplus.entity.dto.osu.beatmap.ScoreDTO;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class BeatmapUserScore implements Serializable
{
    private Integer position;
    private ScoreDTO score;
}
