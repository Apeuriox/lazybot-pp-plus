package me.aloic.lazybotppplus.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.aloic.lazybotppplus.entity.dto.osu.optional.beatmap.ScoreStatisticsLazer;

import java.util.Optional;

@Data
@TableName("score_statistics")
@AllArgsConstructor
@NoArgsConstructor
public class ScoreStatisticsPO {

    private Long scoreId;
    private Integer count300;
    private Integer count100;
    private Integer count50;
    private Integer count0;
    private Integer countTick;
    private Integer countEnd;

    public ScoreStatisticsPO(ScoreStatisticsLazer statisticsLazer, Long scoreId) {
        this.scoreId=scoreId;
        this.count300=statisticsLazer.getGreat();
        this.count100=statisticsLazer.getOk();
        this.count50=statisticsLazer.getMeh();
        this.count0=statisticsLazer.getMiss();
        this.countTick= Optional.ofNullable(statisticsLazer.getLarge_tick_hit()).orElse(0);
        this.countEnd= Optional.ofNullable(statisticsLazer.getSlider_tail_hit()).orElse(0);
    }
}
