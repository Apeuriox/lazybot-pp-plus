package me.aloic.lazybotppplus.entity.dto.osu.optional.player;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class UserMonthlyPlaycount implements Serializable {
    private String start_date;
    private Integer count;
}
