package me.aloic.lazybotppplus.util;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.CharsetUtil;
import me.aloic.lazybotppplus.enums.OsuMode;

import java.util.List;
import java.util.Objects;

public class URLBuildUtil
{
    private static final String BASE_URL = "https://osu.ppy.sh/api/v2";

    public static String buildURLOfBeatmapScore(String beatmapId, String playerId, OsuMode mode)
    {
        UrlBuilder builder = UrlBuilder.ofHttp(BASE_URL, CharsetUtil.CHARSET_UTF_8);
        builder.addPath("beatmaps")
                .addPath(beatmapId)
                .addPath("scores")
                .addPath("users")
                .addPath(playerId)
                .addQuery("mode", mode.getDescribe());
        return builder.build();
    }
    public static String buildURLOfBeatmapScoreAll(String beatmapId, String playerId, OsuMode mode)
    {
        UrlBuilder builder = UrlBuilder.ofHttp(BASE_URL, CharsetUtil.CHARSET_UTF_8);
        builder.addPath("beatmaps")
                .addPath(beatmapId)
                .addPath("scores")
                .addPath("users")
                .addPath(playerId)
                .addPath("all")
                .addQuery("ruleset", mode.getDescribe());
        return builder.build();
    }
    public static String buildURLOfBeatmapScore(String beatmapId, String playerId,String[] modsArray,OsuMode mode)
    {
        UrlBuilder builder = UrlBuilder.ofHttp(BASE_URL, CharsetUtil.CHARSET_UTF_8);
        builder.addPath("beatmaps")
                .addPath(beatmapId)
                .addPath("scores")
                .addPath("users")
                .addPath(playerId);
        for (String s : modsArray)
        {
            builder.addQuery("mods[]", s);
        }
        builder.addQuery("mode", mode.getDescribe());
        return builder.build();
    }
    public static String buildURLOfBeatmapScore(String beatmapId, String playerId,List<String> modsArray,OsuMode mode)
    {
        UrlBuilder builder = UrlBuilder.ofHttp(BASE_URL, CharsetUtil.CHARSET_UTF_8)
                .addPath("beatmaps")
                .addPath(beatmapId)
                .addPath("scores")
                .addPath("users")
                .addPath(playerId);
        for (String s : modsArray)
        {
            builder.addQuery("mods[]", s);
        }
        builder.addQuery("mode", mode.getDescribe());
        return builder.build();
    }
    public static String buildURLOfBeatmap(String beatmapId,OsuMode mode)
    {
        UrlBuilder builder = UrlBuilder.ofHttp(BASE_URL, CharsetUtil.CHARSET_UTF_8)
                .addPath("beatmaps")
                .addPath(beatmapId)
                .addQuery("mode", mode.getDescribe());
        return builder.build();
    }
    public static String buildURLOfBeatmap(String beatmapId)
    {
       return buildURLOfBeatmap(beatmapId, OsuMode.Osu);
    }
    public static String buildURLOfUserBest(String playerId, Integer offset,OsuMode mode)
    {
        UrlBuilder builder = UrlBuilder.ofHttp(BASE_URL, CharsetUtil.CHARSET_UTF_8)
                .addPath("users")
                .addPath(playerId)
                .addPath("scores")
                .addPath("best")
                .addQuery("limit", 1)
                .addQuery("offset", offset)
                .addQuery("mode", mode.getDescribe());
        return builder.build();
    }
    public static String buildURLOfUserBest(String playerId,Integer limit, Integer offset, OsuMode mode)
    {
        UrlBuilder builder = UrlBuilder.ofHttp(BASE_URL, CharsetUtil.CHARSET_UTF_8)
                .addPath("users")
                .addPath(playerId)
                .addPath("scores")
                .addPath("best")
                .addQuery("limit", limit)
                .addQuery("offset", offset)
                .addQuery("mode", mode.getDescribe());
        return builder.build();
    }
    public static String buildURLOfRecentCommand(Integer playerId, Integer type, Integer limit ,OsuMode mode)
    {
        UrlBuilder builder = UrlBuilder.ofHttp(BASE_URL, CharsetUtil.CHARSET_UTF_8)
                .addPath("users")
                .addPath(String.valueOf(playerId))
                .addPath("scores")
                .addPath("recent")
                .addQuery("mode", mode.getDescribe())
                .addQuery("limit", limit);
                if(type!=1)
                     builder.addQuery("include_fails", 1);
        return builder.build();
    }
    public static String buildURLOfPlayerInfo(String playerNameOrId, OsuMode mode,String key)
    {
        UrlBuilder builder = UrlBuilder.ofHttp(BASE_URL, CharsetUtil.CHARSET_UTF_8);
        playerNameOrId = Objects.equals(key, "username") ? "@".concat(playerNameOrId):playerNameOrId;
        builder.addPath("users")
                .addPath(playerNameOrId)
                .addPath(mode.getDescribe());
        return builder.build();
    }
    public static String buildURLOfPlayerInfo(String playerName,OsuMode mode)
    {
        UrlBuilder builder = UrlBuilder.ofHttp(BASE_URL, CharsetUtil.CHARSET_UTF_8)
                .addPath("users")
                .addPath("@".concat(playerName))
                .addPath(mode.getDescribe());
        return builder.build();
    }
    public static String buildURLOfPlayerInfo(Integer playerId,OsuMode mode)
    {
        UrlBuilder builder = UrlBuilder.ofHttp(BASE_URL, CharsetUtil.CHARSET_UTF_8)
                .addPath("users")
                .addPath(String.valueOf(playerId))
                .addPath(mode.getDescribe());
        return builder.build();
    }
    public static String buildURLOfPlayerInfo(String playerName)
    {
        UrlBuilder builder = UrlBuilder.ofHttp(BASE_URL, CharsetUtil.CHARSET_UTF_8)
                .addPath("users")
                .addPath("@".concat(playerName));
        return builder.build();
    }
    public static String buildURLOfPlayerInfoArray(List<String> playerIds)
    {
        UrlBuilder builder = UrlBuilder.ofHttp("https://osu.ppy.sh/api/v2/users", CharsetUtil.CHARSET_UTF_8);
        for(String id:playerIds)
        {
            builder.addQuery("ids[]",id);
        }
        return builder.build();
    }

    public static String buildURLOfBestPerformance(int playerId)
    {
        return BASE_URL + "/users/" + playerId + "/scores/best?limit=100";
    }


}
