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
        logger.info("正在初始化静态资源");
        try{
            String workingDir = System.getenv("LAZYBOT_DIR");
            if (workingDir == null || workingDir.isEmpty()) {
                workingDir = String.valueOf(Files.createTempDirectory("lazybot_working_dir"));
            }
            File targetDir = new File(workingDir);
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                throw new IOException("无法创建目标目录：" + workingDir);
            }
            resourcePath = Paths.get(targetDir.getAbsolutePath());
            createOsuDirectories(resourcePath);
            logger.info("资源路径获取成功：{}", targetDir.getAbsolutePath());
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
            throw new LazybotRuntimeException("释放静态资源时出错: " + e.getMessage());
        }
    }


    public static Path getResourcePath() {
        if (resourcePath == null) {
            throw new IllegalStateException("工作目录未初始化！");
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
                throw new IOException("无法创建目录：" + dir.getAbsolutePath());
            }
            logger.info("目录已创建：{}", dir.getAbsolutePath());
        } else {
            logger.info("目录已存在：{}", dir.getAbsolutePath());
        }
    }

}
