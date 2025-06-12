package me.aloic.lazybotppplus.util;

import me.aloic.lazybotppplus.exception.LazybotRuntimeException;
import me.aloic.lazybotppplus.monitor.ResourceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.*;

public class AssertDownloadUtil
{
    private static final Logger logger = LoggerFactory.getLogger(AssertDownloadUtil.class);
    private static final int MAX_DOWNLOADS_PER_MINUTE;
    private static final long ONE_MINUTE_IN_MS;
    private static final DelayQueue<DownloadTask> delayQueue;
    private static final ExecutorService executor;
    private static final HttpClient httpClient;
    private static final int MAX_RETRIES;
    private static final String BEATMAP_DOWNLOAD_URL = "https://osu.ppy.sh/osu";

    static{
        MAX_DOWNLOADS_PER_MINUTE=64;
        ONE_MINUTE_IN_MS=60*1000;
        delayQueue=new DelayQueue<>();
        executor=Executors.newScheduledThreadPool(6);
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        MAX_RETRIES=3;
        initDownloadControl();

    }
    private static void initDownloadControl() {
        for (int i = 0; i < MAX_DOWNLOADS_PER_MINUTE; i++) {
            delayQueue.offer(new DownloadTask(1000));
        }
    }

    public static void downloadResourceQueue(String targetUrl, String desiredLocalPath) throws InterruptedException, ExecutionException {
        Future<Void> downloadFuture = executor.submit(() -> {
            try {
                delayQueue.take();
                fileDownloadJavaHttpClient(targetUrl, desiredLocalPath);
                delayQueue.offer(new DownloadTask(ONE_MINUTE_IN_MS));
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
                throw new LazybotRuntimeException("Download thread was interrupted: " +e.getMessage());
            }
            return null;
        });
        downloadFuture.get();
        logger.info("(QUEUE) Download completed for: {}", targetUrl);
    }
    private static void downloadResourceSmallQueue(String targetUrl, String desiredLocalPath) throws InterruptedException, ExecutionException {
        Future<Void> downloadFuture = executor.submit(() -> {
            try {
                delayQueue.take();
                fileDownloadJavaHttpClient(targetUrl, desiredLocalPath);
                delayQueue.offer(new DownloadTask(ONE_MINUTE_IN_MS));
            } catch (InterruptedException e) {
                logger.error("{} : {}",e.getClass(), e.getMessage());
            }
            return null;
        });
        downloadFuture.get();
        logger.info("Download completed for: {}", targetUrl);
    }
    public static boolean beatmapDownload(Integer bid,Boolean override)
    {
        String desiredLocalPath= ResourceMonitor.getResourcePath().toAbsolutePath()+ "/" + bid +".osu";
        File saveFilePath = new File(desiredLocalPath);
        if (saveFilePath.exists() && !override) {
            logger.info("Beatmap file .osu existing: {}", saveFilePath.getAbsolutePath());
            return false;
        }
        String targetUrl= BEATMAP_DOWNLOAD_URL+ "/" +bid;
        try{
            downloadResourceSmallQueue(targetUrl,desiredLocalPath);
        }
        catch (Exception e)
        {
            logger.error("Beatmap download failed: {}", e.getMessage());
        }

        return true;
    }


    public static Path beatmapPath(Integer bid,Boolean override)
    {
        beatmapDownload(bid,override);
        return Paths.get(ResourceMonitor.getResourcePath().toAbsolutePath()+ "/" +bid +".osu");
    }



    private static void fileDownloadJavaHttpClient(String targetUrl, String desiredLocalPath) throws Exception {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                attempt++;
                logger.info("Trying to download file ({} times)： {} to {}", attempt, targetUrl, desiredLocalPath);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(targetUrl))
                        .timeout(Duration.ofSeconds(30))
                        .GET()
                        .build();
                Path path = Path.of(desiredLocalPath);
                HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(path));
                if (response.statusCode() == 200) {
                    logger.info("File download successful，save path：{}", desiredLocalPath);
                    return;
                } else {
                    throw new LazybotRuntimeException("HTTP status code：" + response.statusCode());
                }
            } catch (Exception e) {
                logger.warn("File download failed ({} times): {}", attempt, e.getMessage());
                if (attempt >= MAX_RETRIES) {
                    logger.error("Failed to download file after 3 retries");
                    throw new LazybotRuntimeException("Failed to download file after 3 retries: " + e.getMessage());
                }
                Thread.sleep(2000);
            }
        }
    }


    static class DownloadTask implements Delayed {
        private long delayTime;
        private long createdTime;

        public DownloadTask(long delayTime) {
            this.delayTime = delayTime;
            this.createdTime = System.currentTimeMillis();
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long elapsed = System.currentTimeMillis() - createdTime;
            return unit.convert(delayTime - elapsed, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return Long.compare(this.createdTime, ((DownloadTask) o).createdTime);
        }
    }

}
