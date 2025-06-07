package me.aloic.lazybotppplus.entity.dto.osu.player;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.aloic.lazybotppplus.entity.dto.osu.optional.player.Description;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class GroupDTO implements Serializable {
    private String colour;
    private Boolean has_listing;
    private Boolean has_playmodes;
    private Integer id;
    private String identifier;
    private Boolean is_probationary;
    private String name;
    private String short_name;
    private Description description;
}
