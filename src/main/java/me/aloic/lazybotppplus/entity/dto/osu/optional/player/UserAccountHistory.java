package me.aloic.lazybotppplus.entity.dto.osu.optional.player;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class UserAccountHistory implements Serializable {
    private String description;
    private Integer id;
    private Long length;
    private Boolean permanent;
    private String timestamp;
    private String type;

}
