package me.aloic.lazybotppplus.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.aloic.lazybotppplus.entity.po.ScorePO;
import me.aloic.lazybotppplus.entity.vo.PPPlusPerformance;
import me.aloic.lazybotppplus.exception.LazybotRuntimeException;

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
    public static PerformanceDimension fromString(String dimension) {
        try {
            return PerformanceDimension.valueOf(dimension);
        } catch (IllegalArgumentException e) {
            throw new LazybotRuntimeException("Invalid performance dimension: " + dimension);
        }
    }
    public static PerformanceDimension getDimension(String dimension) {
        if (dimension == null) throw new LazybotRuntimeException("Null dimension provided");
        return switch (dimension.toLowerCase().trim())
        {
            case "aim", "0" -> AIM;
            case "jump", "acute", "1", "j" -> JUMP;
            case "flow", "f", "2" -> FLOW;
            case "speed", "spd", "3" -> SPEED;
            case "stamina", "sta", "4" -> STAMINA;
            case "precision", "pre", "5" -> PRECISION;
            case "accuracy", "acc", "6" -> ACCURACY;
            case "pp", "total", "7" -> PP;
            default -> throw new LazybotRuntimeException("Invalid dimension provided: " + dimension);
        };
    }

}
