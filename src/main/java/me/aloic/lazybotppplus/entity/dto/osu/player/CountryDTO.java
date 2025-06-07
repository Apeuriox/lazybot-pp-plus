package me.aloic.lazybotppplus.entity.dto.osu.player;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class CountryDTO implements Serializable {
    private String code;
    private String name;
}
