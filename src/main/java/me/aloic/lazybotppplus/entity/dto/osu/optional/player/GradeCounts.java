package me.aloic.lazybotppplus.entity.dto.osu.optional.player;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class GradeCounts implements Serializable {
    private Integer ss;
    private Integer ssh;
    private Integer s;
    private Integer sh;
    private Integer a;

}
