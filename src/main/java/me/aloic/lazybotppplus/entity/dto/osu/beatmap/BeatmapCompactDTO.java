package me.aloic.lazybotppplus.entity.dto.osu.beatmap;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.aloic.lazybotppplus.entity.dto.osu.optional.beatmap.Failtimes;


import java.io.Serializable;

@Data
@NoArgsConstructor
public class BeatmapCompactDTO implements Serializable {
    private Integer beatmapset_id;
    private Double difficulty_rating;
    private Integer id;
    private String mode;
    private String status;
    private Integer total_length;
    private Integer user_id;
    private String version;
    private String checksum;
    private Failtimes failtimes;
    private Integer max_combo;
    private BeatmapsetDTO beatmapset;
}
