package me.aloic.lazybotppplus.entity.dto.osu.player;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.aloic.lazybotppplus.entity.dto.osu.beatmap.ScoreLazerDTO;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class BeatmapUserScores implements Serializable
{
    private List<ScoreLazerDTO> scores;
}