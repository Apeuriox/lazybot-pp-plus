package me.aloic.lazybotppplus.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.aloic.lazybotppplus.entity.po.ScorePO;
import me.aloic.lazybotppplus.entity.vo.PPPlusPerformance;

import java.util.function.BiConsumer;
import java.util.function.Function;

@Getter
public enum PerformanceDimension
{
    PP("pp", ScorePO::getPp, PPPlusPerformance::setPp),
    SPEED("pp_speed", ScorePO::getPpSpeed, PPPlusPerformance::setPpSpeed),
    STAMINA("pp_stamina", ScorePO::getPpStamina, PPPlusPerformance::setPpStamina),
    PRECISION("pp_precision", ScorePO::getPpPrecision, PPPlusPerformance::setPpPrecision),
    AIM("pp_aim", ScorePO::getPpAim, PPPlusPerformance::setPpAim),
    JUMP("pp_jump", ScorePO::getPpJump, PPPlusPerformance::setPpJumpAim),
    FLOW("pp_flow", ScorePO::getPpFlow, PPPlusPerformance::setPpFlowAim),
    ACCURACY("pp_accuracy", ScorePO::getPpAccuracy, PPPlusPerformance::setPpAcc);

    private final String dbColumn;
    private final Function<ScorePO, Double> getter;
    private final BiConsumer<PPPlusPerformance, Double> setter;

    PerformanceDimension(String dbColumn, Function<ScorePO, Double> getter, BiConsumer<PPPlusPerformance, Double> setter) {
        this.dbColumn = dbColumn;
        this.getter = getter;
        this.setter = setter;
    }

}
