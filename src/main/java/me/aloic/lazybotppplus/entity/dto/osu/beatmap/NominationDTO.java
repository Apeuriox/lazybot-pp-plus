package me.aloic.lazybotppplus.entity.dto.osu.beatmap;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class NominationDTO implements Serializable
{
    private Integer beatmapset_id;
    private List<RulesetDTO> rulesets;
    private Boolean reset;
    private Integer user_id;
}
