package me.aloic.lazybotppplus.entity.dto.osu.optional.player;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class UserStatisticsRulesets implements Serializable {
    private StatisticsRuleSet osu;
    private StatisticsRuleSet taiko;
    private StatisticsRuleSet fruits;
    private StatisticsRuleSet mania;

}
