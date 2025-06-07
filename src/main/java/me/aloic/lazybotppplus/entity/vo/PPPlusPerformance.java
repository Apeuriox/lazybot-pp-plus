package me.aloic.lazybotppplus.entity.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class PPPlusPerformance
{
    private double pp;
    private double ppAim;
    private double ppJumpAim;
    private double ppFlowAim;
    private double ppPrecision;
    private double ppSpeed;
    private double ppStamina;
    private double ppAcc;
    @JsonIgnore
    private double effectiveMissCount;

    @Override
    public String toString()
    {
        return "PPPlusPerformance{" +
                "pp=" + pp +
                ", ppAim=" + ppAim +
                ", ppJumpAim=" + ppJumpAim +
                ", ppFlowAim=" + ppFlowAim +
                ", ppPrecision=" + ppPrecision +
                ", ppSpeed=" + ppSpeed +
                ", ppStamina=" + ppStamina +
                ", ppAcc=" + ppAcc +
                ", effectiveMissCount=" + effectiveMissCount +
                '}';
    }
}
