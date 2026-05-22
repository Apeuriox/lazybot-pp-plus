package me.aloic.lazybotppplus.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.aloic.lazybotppplus.entity.dto.osu.beatmap.ScoreLazerDTO;
import me.aloic.lazybotppplus.entity.po.ScorePO;

import java.io.Serializable;

@Data

@NoArgsConstructor
public class ScoreUpdateVO implements Serializable
{
    private ScorePO scoreInDb;
    private ScoreLazerDTO originalScore;
    private Double ppplus;
    private Integer beatmapId;

    public ScoreUpdateVO(ScorePO scorePO, ScoreLazerDTO scoreLazerDTO)
    {
        this.scoreInDb=scorePO;
        this.originalScore=scoreLazerDTO;
        this.ppplus=scorePO.getPp();
        this.beatmapId=scoreLazerDTO.getBeatmap_id();
    }
}
