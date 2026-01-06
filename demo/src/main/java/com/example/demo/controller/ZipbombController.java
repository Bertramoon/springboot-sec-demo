package com.example.demo.controller;

import com.example.demo.vo.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/zipbomb")
public class ZipbombController {
    private static final int MAX_ENTRIES = 1000;
    private static final int MAX_SINGLE_FILE_SIZE = 6 * 1024 * 1024;
    private static final int MAX_TOTAL_SIZE = 20 * 1024 * 1024;
    private static final Logger logger = LoggerFactory.getLogger(ZipbombController.class);


    /**
     * 不安全解压
     * 1、没有检查条目数量，存在IO解压DoS
     * 2、没有检查文件名和文件扩展名，存在跨目录写入和任意文件类型写入
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/unsafe-unzip")
    public Response<?> unzipUnsafe(@RequestPart MultipartFile file) throws IOException {
        List<File> extractedFiles = new ArrayList<>();
        long totalExtracted = 0L;
        File outputDirFile = new File(UUID.randomUUID().toString());
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs();
        }

        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            byte[] buffer = new byte[4096];

            while ((entry = zis.getNextEntry()) != null) {
                // 创建目标文件
                File targetFile = new File(outputDirFile, entry.getName());

                // 如果是目录，创建并继续
                if (entry.isDirectory()) {
                    targetFile.mkdirs();
                    zis.closeEntry();
                    continue;
                }

                // 检查单个文件大小
                if (entry.getSize() > MAX_SINGLE_FILE_SIZE) {
                    deleteExtractedFiles(extractedFiles);
                    outputDirFile.delete();
                    throw new SecurityException(
                            String.format("文件 %s 大小超过限制 (%d > %d)",
                                    entry.getName(), entry.getSize(), MAX_SINGLE_FILE_SIZE));
                }
                // 检查总大小
                totalExtracted += entry.getSize();
                if (totalExtracted > MAX_TOTAL_SIZE) {
                    deleteExtractedFiles(extractedFiles);
                    outputDirFile.delete();
                    throw new SecurityException(
                            String.format("总解压大小超过限制 (%d > %d)",
                                    totalExtracted, MAX_TOTAL_SIZE));
                }
                // 确保父目录存在
                targetFile.getParentFile().mkdirs();
                try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                    int bytesRead;
                    while ((bytesRead = zis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    extractedFiles.add(targetFile);
                }
                zis.closeEntry();
            }
        }
        return Response.success();
    }

    @PostMapping("/safe-unzip")
    public Response<?> unzip(@RequestPart MultipartFile file) throws IOException {
        // 其他基础检查
        // ...

        List<File> extractedFiles = new ArrayList<>();
        int entryCount = 0;
        long totalExtracted = 0L;
        File outputDirFile = new File(UUID.randomUUID().toString());
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs();
        }

        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            byte[] buffer = new byte[4096];

            while ((entry = zis.getNextEntry()) != null) {
                entryCount++;

                // 检查条目数量
                if (entryCount > MAX_ENTRIES) {
                    throw new SecurityException("ZIP包含的文件数量超过限制: " + entryCount);
                }

                // 检查文件名和文件扩展名，避免跨目录和任意文件类型写入
                if (!Pattern.matches("^[\\w()\\[\\]/ ]{1,64}\\.(txt|md|csv|xls|xlsx|doc|docx|ppt|xml|bin)$", entry.getName())) {
                    throw new SecurityException("文件名不符合要求");
                }

                // 创建目标文件
                File targetFile = new File(outputDirFile, entry.getName());

                // 防止路径遍历
                if (!targetFile.getCanonicalPath().startsWith(outputDirFile.getCanonicalPath())) {
                    throw new SecurityException("非法文件路径: " + entry.getName());
                }

                // 如果是目录，创建并继续
                if (entry.isDirectory()) {
                    targetFile.mkdirs();
                    zis.closeEntry();
                    continue;
                }

                // 确保父目录存在
                targetFile.getParentFile().mkdirs();

                // 边解压边检查
                try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                    long entryExtracted = 0;
                    int bytesRead;

                    while ((bytesRead = zis.read(buffer)) != -1) {
                        // 检查单个文件大小
                        entryExtracted += bytesRead;
                        if (entryExtracted > MAX_SINGLE_FILE_SIZE) {
                            deleteExtractedFiles(extractedFiles);
                            outputDirFile.delete();
                            throw new SecurityException(
                                    String.format("文件 %s 大小超过限制 (%d > %d)",
                                            entry.getName(), entryExtracted, MAX_SINGLE_FILE_SIZE));
                        }

                        // 检查总大小
                        totalExtracted += bytesRead;
                        if (totalExtracted > MAX_TOTAL_SIZE) {
                            deleteExtractedFiles(extractedFiles);
                            outputDirFile.delete();
                            throw new SecurityException(
                                    String.format("总解压大小超过限制 (%d > %d)",
                                            totalExtracted, MAX_TOTAL_SIZE));
                        }

                        fos.write(buffer, 0, bytesRead);
                    }

                    extractedFiles.add(targetFile);
                    logger.info("成功解压文件: {} ({} bytes)", entry.getName(), entryExtracted);
                }
                zis.closeEntry();
            }
        }
        return Response.success();
    }

    /**
     * 删除已解压的文件（发生错误时清理）
     */
    private void deleteExtractedFiles(List<File> files) {
        for (File file : files) {
            try {
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                logger.error("删除文件失败: {}", file.getAbsolutePath(), e);
            }
        }
    }
}
