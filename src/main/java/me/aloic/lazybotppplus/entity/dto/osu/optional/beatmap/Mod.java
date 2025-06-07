package me.aloic.lazybotppplus.entity.dto.osu.optional.beatmap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Mod implements Serializable
{
    private String acronym;
    private ModSetting settings;
}
