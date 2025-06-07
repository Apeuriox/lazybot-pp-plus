package me.aloic.lazybotppplus.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.aloic.lazybotppplus.entity.dto.osu.beatmap.BeatmapDTO;
import me.aloic.lazybotppplus.entity.dto.osu.beatmap.BeatmapsetDTO;

@Data
@TableName("beatmap")
@AllArgsConstructor
@NoArgsConstructor
public class BeatmapPO {
    @TableId(type = IdType.INPUT)
    private Long id;

    private String title;
    private String artist;
    private String version;
    private Double bpm;
    private String checksum;

    public BeatmapPO(BeatmapDTO beatmap, BeatmapsetDTO beatmapset) {
        this.id= Long.valueOf(beatmap.getId());
        this.title=beatmapset.getTitle_unicode();
        this.artist=beatmapset.getArtist_unicode();
        this.version=beatmap.getVersion();
        this.bpm=beatmap.getBpm();
        this.checksum=beatmap.getChecksum();
    }
}
