package me.aloic.lazybotppplus.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.aloic.lazybotppplus.entity.dto.osu.optional.beatmap.Mod;
import me.aloic.lazybotppplus.entity.dto.osu.optional.beatmap.ModSetting;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum RankedMods
{
    Easy("EZ", null),
    NoFail("NF", null),
    HalfTime("HT", null),
    DayCore("DC", null),
    Nightcore("NC", null),
    DoubleTime("DT", null),
    HardRock("HR", null),
    Flashlight("FL", null),
    Hidden("HD", null),
    SuddenDeath("SD", null),
    Perfect("PF", null),
    SpunOut("SO", null),
    Blinds("BL",null),
    AccuracyChallenge("AC",null),
    Traceable("TC",null),
    Muted("MU",null),
    NoScope("NS",null);


    private final String acronym;
    //when rate time processing was finished this was needed
    private final ModSetting setting;

    public static boolean checkModsRankability(List<Mod> mods) {
        if (mods == null || mods.isEmpty()) {
            return true;
        }
        Map<String, RankedMods> acronymMap = Arrays.stream(RankedMods.values())
                .collect(Collectors.toMap(RankedMods::getAcronym, Function.identity()));
        for (Mod mod : mods) {
            RankedMods rankedMod = acronymMap.get(mod.getAcronym());
            // temp solution, we should compare it with each mod
            if (rankedMod == null || mod.getSettings() != null) {
                return false;
            }
        }

        return true;
    }
}
