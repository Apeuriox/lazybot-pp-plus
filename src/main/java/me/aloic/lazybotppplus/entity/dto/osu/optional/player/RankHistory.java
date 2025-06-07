package me.aloic.lazybotppplus.entity.dto.osu.optional.player;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class RankHistory implements Serializable {
    private String mode;
    private Integer[] data;

}
