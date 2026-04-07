package me.aloic.lazybotppplus.util;

import java.util.concurrent.TimeUnit;

//google go fuck your coders. your guava-rate-limiter/15.0-atlassian-1 need Ticker class which located in com.google.common.base package. that means you need to install the full version of guava.
// maybe youre wondering why i dont just simply import com.google.common.base? the hell do you know some other dependencies are being used in that Ticker class?
// as my requirements i decided to code a simple one for myself
public class RateLimiter
{
    private final long intervalNanos;
    private long nextFreeTicketNanos = System.nanoTime();


    public RateLimiter(double permitsPerSecond) {
        this.intervalNanos = (long) (TimeUnit.SECONDS.toNanos(1) / permitsPerSecond);
    }

    public synchronized void acquire() {
        long now = System.nanoTime();

        if (now > nextFreeTicketNanos) {
            nextFreeTicketNanos = now;
        }

        long waitTime = nextFreeTicketNanos - now;
        nextFreeTicketNanos += intervalNanos;

        if (waitTime > 0) {
            try {
                TimeUnit.NANOSECONDS.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}