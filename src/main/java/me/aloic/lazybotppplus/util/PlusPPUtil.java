package me.aloic.lazybotppplus.util;

import cn.hutool.json.JSONUtil;
import desu.life.RosuFFI;
import me.aloic.lazybotppplus.entity.dto.osu.beatmap.ScoreLazerDTO;
import me.aloic.lazybotppplus.entity.dto.osu.optional.beatmap.Mod;
import me.aloic.lazybotppplus.entity.dto.osu.optional.beatmap.ScoreStatisticsLazer;
import me.aloic.lazybotppplus.entity.vo.PPPlusPerformance;
import me.aloic.lazybotppplus.entity.vo.ScoreVO;
import me.aloic.lazybotppplus.exception.LazybotRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.Collectors;

public class PlusPPUtil
{
    private static final Logger logger = LoggerFactory.getLogger(PlusPPUtil.class);

    public static PPPlusPerformance calcPPPlusStats(String pathToOsuFile, ScoreVO scoreVO) throws RosuFFI.FFIException
    {
        try(RosuFFI.Beatmap beatmap = new RosuFFI.Beatmap(pathToOsuFile)) {
            return calcPPPlusStats(beatmap,
                    scoreVO.getMods().stream()
                            .map(Mod::getAcronym)
                            .collect(Collectors.joining()),
                    scoreVO.getStatistics(),
                    scoreVO.getMaxCombo(),
                    scoreVO.getIsLazer());
        }

    }
    public static PPPlusPerformance calcPPPlusStats(String pathToOsuFile, ScoreLazerDTO score) throws RosuFFI.FFIException
    {
        try(RosuFFI.Beatmap beatmap = new RosuFFI.Beatmap(pathToOsuFile)) {
            return calcPPPlusStats(beatmap,
                    score.getMods().stream()
                            .map(Mod::getAcronym)
                            .collect(Collectors.joining()),
                    score.getStatistics(),
                    score.getMax_combo(),
                    score.getLegacy_total_score() == 0);
        }

    }
    private static PPPlusPerformance calcPPPlusStats(RosuFFI.Beatmap beatmap, String modAcronyms, ScoreStatisticsLazer statistics, Integer maxCombo, boolean isLazerScore)
    {
        PPPlusPerformance resultPerformance=new PPPlusPerformance();
        try( RosuFFI.Performance performance  = new RosuFFI.Performance()) {
            if(maxCombo!=0)
                performance.setCombo(maxCombo);
            performance.setMode(RosuFFI.Mode.Osu);
            performance.setN300(Optional.ofNullable(statistics.getGreat()).orElse(0));
            performance.setN100(Optional.ofNullable(statistics.getOk()).orElse(0));
            performance.setN50(Optional.ofNullable(statistics.getMeh()).orElse(0));
            if(maxCombo!=0)
                performance.setMisses(Optional.ofNullable(statistics.getMiss()).orElse(0));
            performance.setLazer(isLazerScore);
            if(isLazerScore) {
                performance.setLargeTickHits(Optional.ofNullable(statistics.getLarge_tick_hit()).orElse(0));
                performance.setSliderEndHits(Optional.ofNullable(statistics.getSlider_tail_hit()).orElse(0));
            }
            performance.setMods(RosuFFI.Mods.fromAcronyms(modAcronyms,RosuFFI.Mode.Osu));
            RosuFFI.RosuPPLib.PerformanceAttributes calcResult = performance.calculate(beatmap);
            resultPerformance.setPp(calcResult.osu.t.pp);
            resultPerformance.setPpAim(calcResult.osu.t.pp_aim);
            resultPerformance.setPpSpeed(calcResult.osu.t.pp_speed);
            resultPerformance.setPpStamina(calcResult.osu.t.pp_stamina);
            resultPerformance.setPpJumpAim(calcResult.osu.t.pp_jump_aim);
            resultPerformance.setPpFlowAim(calcResult.osu.t.pp_flow_aim);
            resultPerformance.setPpPrecision(calcResult.osu.t.pp_precision);
            resultPerformance.setPpAcc(calcResult.osu.t.pp_acc);
            resultPerformance.setEffectiveMissCount(calcResult.osu.t.effective_miss_count);
        }
        catch (RosuFFI.FFIException e) {
            logger.error("计算pp+时出错: {}", e.getMessage());
            throw new LazybotRuntimeException("计算pp+时出错: " + e.getMessage());
        }
        return resultPerformance;
    }
}
