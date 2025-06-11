package me.aloic.lazybotppplus.monitor;

import me.aloic.lazybotppplus.exception.LazybotRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class ResourceMonitor
{
    private static Path resourcePath;

    private static final Logger logger = LoggerFactory.getLogger(ResourceMonitor.class);

    public static void initResources()
    {
        logger.info("Initializing resources...");
        try{
            String workingDir = System.getenv("LAZYBOT_DIR");
            if (workingDir == null || workingDir.isEmpty()) {
                workingDir = String.valueOf(Files.createTempDirectory("lazybot_working_dir"));
            }
            File targetDir = new File(workingDir);
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                throw new IOException("Can not create target folder：" + workingDir);
            }
            resourcePath = Paths.get(targetDir.getAbsolutePath());
//            createOsuDirectories(resourcePath);
            logger.info("Resources path created successfully：{}", targetDir.getAbsolutePath());
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
            throw new LazybotRuntimeException("Initialize resources failed: " + e.getMessage());
        }
    }


    public static Path getResourcePath() {
        if (resourcePath == null) {
            throw new IllegalStateException("resource path did not initialize！");
        }
        return resourcePath;
    }


    public static void createOsuDirectories(Path workingDir) throws IOException {
        Path osuFilesDir = workingDir.resolve("osuFiles");
        createDirectoryIfNotExists(osuFilesDir);
    }


    private static void createDirectoryIfNotExists(Path dirPath) throws IOException {
        File dir = dirPath.toFile();
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new IOException("Can not create target folder：" + dir.getAbsolutePath());
            }
            logger.info("Folder created：{}", dir.getAbsolutePath());
        } else {
            logger.info("Folder existed：{}", dir.getAbsolutePath());
        }
    }

}
