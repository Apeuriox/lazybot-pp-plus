package me.aloic.lazybotppplus.monitor;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.aloic.lazybotppplus.entity.mapper.PlayerSummaryMapper;
import me.aloic.lazybotppplus.entity.po.PlayerSummaryPO;
import me.aloic.lazybotppplus.service.PlayerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PlayerUpdateMonitor
{

    @Resource
    private PlayerService playerService;

    @Resource
    private PlayerSummaryMapper playerSummaryMapper;

    @Scheduled(cron = "0 0 3 * * ?")
    public void schedulePlayerUpdate()
    {
        log.info("Hello Im about to start updating players...");
        long start = System.currentTimeMillis();
        List<PlayerSummaryPO> players = playerSummaryMapper.selectAll();
        if (players == null || players.isEmpty()) {
            log.warn("oops, no players to update");
            return;
        }
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (PlayerSummaryPO player : players) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    playerService.updatePlayerStatsNoResult(player.getId());
                } catch (Exception e) {
                    log.error("Failed to update player {}: {}", player.getId(), e.getMessage(), e);
                }
            });
            futures.add(future);
            try {
                TimeUnit.MILLISECONDS.sleep(15);
            } catch (InterruptedException ignored) {
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long end = System.currentTimeMillis();
        log.info("All players updated！ size: {}", players.size());
        log.info("Total time consumed: {}ms, Avg.: {}ms/player", (end - start), (end - start) / players.size());
    }
}