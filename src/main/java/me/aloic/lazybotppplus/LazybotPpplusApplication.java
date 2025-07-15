package me.aloic.lazybotppplus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LazybotPpplusApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(LazybotPpplusApplication.class, args);
    }
}
