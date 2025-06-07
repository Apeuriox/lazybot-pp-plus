package me.aloic.lazybotppplus.entity.dto.osu.optional.player;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class Cover implements Serializable {
     private String custom_url;
     private Integer id;
     private String url;

}
