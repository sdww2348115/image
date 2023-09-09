package com.sdww8591.image.application;

import com.google.common.primitives.Floats;
import com.sdww8591.image.domain.Image;
import com.sdww8591.image.service.ImageProcessService;
import com.sdww8591.image.service.MilvusService;
import io.milvus.grpc.MutationResult;
import io.milvus.param.R;
import io.milvus.param.dml.InsertParam;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class ImageApplication {

    @Resource
    private ImageProcessService imageProcessService;

    @Resource
    private MilvusService milvusService;

    public void insertImage2Milvus(File file) {

        log.info("开始构建图片领域对象, 文件名:{}", file.getName());
        Image image = imageProcessService.buildImageFromFile(file);

        log.info("开始执行图片插入");
        milvusService.insertImage2Milvus(image);

        log.info("完成图片插入");
    }

}
