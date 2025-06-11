package me.aloic.lazybotppplus.config;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;
import me.aloic.lazybotppplus.monitor.ResourceMonitor;
import me.aloic.lazybotppplus.monitor.TokenMonitor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InitializeConfig implements ApplicationRunner
{

    @Resource
    private TokenMonitor tokenMonitor;
    @Value("${security.white-list}")
    private Boolean whiteListEnabled;
    @Value("${rate-limit.enabled}")
    private Boolean rateLimitEnabled;

    @Override
    public void run(ApplicationArguments args)
    {
        ResourceMonitor.initResources();
        tokenMonitor.refreshClientToken();
        log.info("IP White list enabled: {}", whiteListEnabled);
        log.info("Rate limit enabled: {}", rateLimitEnabled);
    }
}
