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

    public void insertImage2Milvus(String path) {
        Image image = imageProcessService.buildImageFromFile(path);

        milvusService.insertImage2Milvus(image);
    }

}
