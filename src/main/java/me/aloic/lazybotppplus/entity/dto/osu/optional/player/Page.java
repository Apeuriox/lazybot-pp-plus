package me.aloic.lazybotppplus.entity.dto.osu.optional.player;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class Page implements Serializable {
    private String html;
    private String raw;

}
