package me.aloic.lazybotppplus.entity.vo;

import lombok.Data;

@Data
public class ApiUsageStats {
    private String apiName;

    private long totalCount;
    private long successCount;
    private long errorCount;

    private long totalLatency;

    public double getAvgLatency() {
        return this.totalCount == 0 ? 0 : (double) this.totalLatency / this.totalCount;
    }
}