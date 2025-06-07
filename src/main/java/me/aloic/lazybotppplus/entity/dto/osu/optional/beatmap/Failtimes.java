package me.aloic.lazybotppplus.entity.dto.osu.optional.beatmap;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class Failtimes implements Serializable {
    private List<Integer> exit;
    private List<Integer> fail;
}
