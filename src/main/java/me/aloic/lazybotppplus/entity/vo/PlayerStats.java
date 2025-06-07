package me.aloic.lazybotppplus.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStats
{
    private Long id;
    private PPPlusPerformance performances;
}
