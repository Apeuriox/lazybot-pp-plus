package me.aloic.lazybotppplus.entity.dto.osu.player;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.aloic.lazybotppplus.entity.dto.osu.optional.player.*;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class UserCompactDTO implements Serializable {
    private String avatar_url;

    private String country_code;

    private String default_group;

    private Integer id;

    private Boolean is_active;

    private Boolean is_bot;

    private Boolean is_deleted;

    private Boolean is_online;

    private Boolean is_supporter;

    private String last_visit;

    private Boolean pm_friends_only;

    private Integer profile_hue;

    private String username;

    private List<UserAccountHistory> account_history;
    private ProfileBanner active_tournament_banner;
    private List<UserBadge> badges;
    private Integer beatmap_playcounts_count;
    private CountryDTO country;
    private Cover cover;
    private Integer favourite_beatmapset_count;
    private Integer follower_count;
    private Integer graveyard_beatmapset_count;
    private List<UserGroupDTO> groups;
    private Boolean is_restricted;
    private Integer loved_beatmapset_count;
    private List<UserMonthlyPlaycount> monthly_playcounts;
    private Page page;
    private Integer pending_beatmapset_count;
    private List<String> previous_usernames;
    private RankHighest rank_highest;
    private Integer ranked_beatmapset_count;
    private List<ReplaysWatchedCounts> replays_watched_counts;
    private Long scores_best_count;
    private Long scores_first_count;
    private Long scores_recent_count;
    private List<Statistics> statistics;
    private UserStatisticsRulesets statistics_rulesets;
    private Integer support_level;
    private List<UserAchivement> user_achivements;
    private RankHistory rank_history;
    private Integer unread_pm_count;
}
