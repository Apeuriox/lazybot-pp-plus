package me.aloic.lazybotppplus.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetaStatistics
{
    private Integer totalPlayers;
    private Integer totalRecordedScores;
    private Integer totalBeatmaps;
}
