package me.aloic.lazybotppplus.monitor;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.aloic.lazybotppplus.entity.mapper.PlayerSummaryMapper;
import me.aloic.lazybotppplus.entity.po.PlayerSummaryPO;
import me.aloic.lazybotppplus.service.PlayerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Component
public class PlayerUpdateMonitor
{

    @Resource
    private PlayerService playerService;

    @Resource
    private PlayerSummaryMapper playerSummaryMapper;

    private static final int PAGE_SIZE = 100;

    //upd
    @Scheduled(cron = "0 0 3 * * ?")
    public void schedulePlayerUpdate()
    {
        log.info("Hello Im about to start updating players...");
        long start = System.currentTimeMillis();

        int currentPage = 1;
        int totalUpdatedPlayers = 0;
        ExecutorService executor = Executors.newFixedThreadPool(25);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
//        Set<Long> failedPlayerIds = ConcurrentHashMap.newKeySet();
        while (true) {
            List<PlayerSummaryPO> players = playerSummaryMapper.selectPlayersWithLimit(PAGE_SIZE * ( currentPage - 1 ), PAGE_SIZE);
            if (players == null || players.isEmpty()) {
                break;
            }
            for (PlayerSummaryPO player : players) {
//                if (failedPlayerIds.contains(player.getId())) continue;
                if (player.getLastUpdated().isBefore(now.minusMonths(2)))
                {
                    log.info("PLayer is inactive, skipping...");
                    continue;
                }
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        playerService.updatePlayerStatsNoResult(player.getId());
                    } catch (Exception e) {
                        log.error("Failed to update player {}: {}", player.getId(), e.getMessage(), e);
//                        failedPlayerIds.add(player.getId());
                    }
                }, executor);
                futures.add(future);
            }
            totalUpdatedPlayers+=players.size();
            currentPage++;
            if (players.size()< PAGE_SIZE) {
                break;
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long end = System.currentTimeMillis();
        log.info("All players updated！ size: {}", totalUpdatedPlayers);
        log.info("Total time consumed: {}ms, Avg.: {}ms/player", (end - start), (end - start) / totalUpdatedPlayers);
    }
}