package me.aloic.lazybotppplus.entity.dto.osu.beatmap;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.aloic.lazybotppplus.entity.dto.osu.optional.beatmap.MaximumStatistics;
import me.aloic.lazybotppplus.entity.dto.osu.optional.beatmap.Mod;
import me.aloic.lazybotppplus.entity.dto.osu.optional.beatmap.ScoreStatisticsLazer;
import me.aloic.lazybotppplus.entity.dto.osu.optional.player.CurrentUserAttributes;
import me.aloic.lazybotppplus.entity.dto.osu.player.PlayerInfoDTO;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class ScoreLazerDTO implements Serializable
{
    private Long classic_total_score;
    private Boolean preserve;
    private Boolean processed;
    private Boolean ranked;
    private MaximumStatistics maximum_statistics;
    private List<Mod> mods;
    private ScoreStatisticsLazer statistics;
    private Integer total_score_without_mods;
    private Integer beatmap_id;
    private Long best_id;
    private Long id;
    private String rank;
    private String type;
    private Integer user_id;
    private Double accuracy;
    private Integer build_id;
    private String ended_at;
    private Boolean has_replay;
    private Boolean is_perfect_combo;
    private Boolean legacy_perfect;
    private Long legacy_score_id;
    private Long legacy_total_score;
    private Integer max_combo;
    private Boolean passed;
    private Double pp;
    private Integer ruleset_id;
    private String started_at;
    private Integer total_score;
    private Boolean replay;
    private CurrentUserAttributes current_user_attributes;
    private BeatmapDTO beatmap;
    private PlayerInfoDTO user;
    private BeatmapsetDTO beatmapset;


    public String[] getModsArray()
    {
        return mods.stream().map(Mod::getAcronym).toArray(String[]::new);
    }


    @Override
    public String toString()
    {
        return "ScoreLazerDTO{" +
                "classic_total_score=" + classic_total_score +
                ", preserve=" + preserve +
                ", processed=" + processed +
                ", ranked=" + ranked +
                ", maximum_statistics=" + maximum_statistics +
                ", mods=" + mods +
                ", statistics=" + statistics +
                ", total_score_without_mods=" + total_score_without_mods +
                ", beatmap_id=" + beatmap_id +
                ", best_id=" + best_id +
                ", id=" + id +
                ", rank='" + rank + '\'' +
                ", type='" + type + '\'' +
                ", user_id=" + user_id +
                ", accuracy=" + accuracy +
                ", build_id=" + build_id +
                ", ended_at='" + ended_at + '\'' +
                ", has_replay=" + has_replay +
                ", is_perfect_combo=" + is_perfect_combo +
                ", legacy_perfect=" + legacy_perfect +
                ", legacy_score_id=" + legacy_score_id +
                ", legacy_total_score=" + legacy_total_score +
                ", max_combo=" + max_combo +
                ", passed=" + passed +
                ", pp=" + pp +
                ", ruleset_id=" + ruleset_id +
                ", started_at='" + started_at + '\'' +
                ", total_score=" + total_score +
                ", replay=" + replay +
                ", current_user_attributes=" + current_user_attributes +
                ", beatmap=" + beatmap +
                ", user=" + user +
                ", beatmapset=" + beatmapset +
                '}';
    }
}
