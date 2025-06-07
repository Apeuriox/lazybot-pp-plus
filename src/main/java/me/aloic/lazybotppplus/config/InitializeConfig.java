package me.aloic.lazybotppplus.config;

import jakarta.annotation.Resource;

import me.aloic.lazybotppplus.monitor.ResourceMonitor;
import me.aloic.lazybotppplus.monitor.TokenMonitor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InitializeConfig implements ApplicationRunner
{

    @Resource
    private TokenMonitor tokenMonitor;

    @Override
    public void run(ApplicationArguments args)
    {
        ResourceMonitor.initResources();
        tokenMonitor.refreshClientToken();
    }
}
