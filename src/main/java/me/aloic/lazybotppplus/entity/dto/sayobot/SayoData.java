package me.aloic.lazybotppplus.entity.dto.sayobot;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class SayoData implements Serializable
{
    private List<Bid_data> bid_data;
    private Integer sid;
    private String title;
    private String artist;
    private String creator;
    private Integer creator_id;
    private Integer favourite_count;
    private Integer language;
    private String genre;
}
