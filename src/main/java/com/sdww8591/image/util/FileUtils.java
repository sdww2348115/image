package com.sdww8591.image.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public class FileUtils {

    private FileUtils(){}

    /**
     * 计算文件MD5值
     * @param file 需要计算的文件
     * @return 文件MD5值
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    @SneakyThrows
    public static String calculateMD5(File file) {

        log.info("开始计算文件MD5值:{}", file.getName());

        MessageDigest md5Digest = MessageDigest.getInstance("MD5");
        try (InputStream is = new FileInputStream(file)) {
            byte[] buffer = new byte[65535];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                md5Digest.update(buffer, 0, bytesRead);
            }
        }

        byte[] md5Bytes = md5Digest.digest();

        // Convert the byte array to a hexadecimal string representation
        StringBuilder hexString = new StringBuilder();
        for (byte md5Byte : md5Bytes) {
            hexString.append(String.format("%02x", md5Byte));
        }

        log.info("文件MD5值计算完成");
        return hexString.toString();
    }

    /**
     * 递归遍历目录
     * @param directory
     * @param consumer
     */
    public static void traverseDirectory(File directory, Consumer<File> consumer) {
        if (!directory.isDirectory()) {
            consumer.accept(directory);
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 如果是子目录，递归遍历
                    traverseDirectory(file, consumer);
                } else {
                    consumer.accept(file);
                }
            }
        }
    }
}
