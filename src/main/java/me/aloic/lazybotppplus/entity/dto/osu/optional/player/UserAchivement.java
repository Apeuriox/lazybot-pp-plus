package me.aloic.lazybotppplus.entity.dto.osu.optional.player;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Data
@NoArgsConstructor
public class UserAchivement implements Serializable {
    private String achieved_at;
    private Integer achievement_id;
}
