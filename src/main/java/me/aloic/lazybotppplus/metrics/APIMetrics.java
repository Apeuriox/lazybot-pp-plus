package me.aloic.lazybotppplus.metrics;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.aloic.lazybotppplus.entity.vo.ApiUsageStats;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

//this class records the count of API calls
@Slf4j
@Getter
@Component
public class APIMetrics
{
    private final ConcurrentHashMap<String, ApiUsageStats> statsMap = new ConcurrentHashMap<>();

    public void recordApiUsage(String apiName, boolean success, long latency) {
        ApiUsageStats stats = statsMap.computeIfAbsent(apiName, k -> new ApiUsageStats());

        synchronized (stats) {
            stats.setApiName(apiName);
            stats.setTotalCount(stats.getTotalCount()+1);
            stats.setTotalLatency(stats.getTotalLatency()+latency);

            if (success) {
                stats.setSuccessCount(stats.getSuccessCount()+1);
            } else {
                stats.setErrorCount(stats.getErrorCount()+1);
            }
        }
    }

    public Map<String, ApiUsageStats> snapshotAndReset() {
        Map<String, ApiUsageStats> snapshot = new HashMap<>();

        statsMap.forEach((k, v) -> {
            synchronized (v) {
                snapshot.put(k, v);
                statsMap.put(k, new ApiUsageStats()); // reset
            }
        });

        return snapshot;
    }
}
