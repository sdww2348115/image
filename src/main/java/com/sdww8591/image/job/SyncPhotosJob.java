package com.sdww8591.image.job;

import com.google.common.base.Preconditions;
import com.sdww8591.image.application.ImageApplication;
import com.sdww8591.image.domain.Image;
import com.sdww8591.image.domain.SearchResult;
import com.sdww8591.image.service.MilvusService;
import com.sdww8591.image.util.FileUtils;
import com.sdww8591.image.util.InvokeUtils;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.sdww8591.image.util.FileUtils.validateFileType;

@Slf4j
@Service
public class SyncPhotosJob {

    @Value("${semilar.threshold}")
    private Float threshold;

    @Resource
    private ImageApplication imageApplication;

    @Resource
    private MilvusService milvusService;

    public void syncPhotos(String srcDirPath, String targetDirPath, String conflictDirPath) {
        Preconditions.checkArgument(new File(srcDirPath).exists());
        Preconditions.checkArgument(new File(targetDirPath).exists());
        Preconditions.checkArgument(new File(conflictDirPath).exists());

        File conflictDir = new File(conflictDirPath);
        File targetDir = new File(targetDirPath);

        FileUtils.traverseDirectory(new File(srcDirPath), file -> {
            if (validateFileType(file)) {
                log.info("开始处理照片:{}", file.getPath());

                List<SearchResult> searchResults = imageApplication.search(file, 5, 1, 5);
                if (CollectionUtils.isNotEmpty(searchResults)) {
                    List<SearchResult> semilarPhotos = searchResults.stream()
                            .filter(searchResult -> searchResult.getDistance() > threshold)
                            .toList();
                    if (CollectionUtils.isNotEmpty(semilarPhotos)) {
                        copyFile2Dir(file, conflictDir);
                        log.info("照片:{}存在高度相似图片:{}", file.getName(), semilarPhotos.stream().map(SearchResult::getImage).map(Image::getName).collect(Collectors.joining(",")));
                        return;
                    }
                }

                copyFile2Dir(file, targetDir);
                imageApplication.insertImage2Milvus(file);
                milvusService.flushIndex();
            } else {
                log.warn("文件:{}无法处理", file.getPath());
            }
        });
    }

    @SneakyThrows
    private void copyFile2Dir(File file, File targetDir) {
        org.apache.commons.io.FileUtils.copyFileToDirectory(file, targetDir);
    }
}
