package com.sdww8591.image.service;

import com.sdww8591.image.TestBootStrap;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Objects;

public class ImageProcessServiceTest extends TestBootStrap {

    @Resource
    private ImageProcessService imageProcessService;

    @Test
    public void extractImageVector() {
        float[] vertex = imageProcessService.extractImageVector(new File("C:\\Users\\sdww\\IdeaProjects\\milvus\\IMG_20220410_174509_1.jpg"));
        Assertions.assertTrue(Objects.nonNull(vertex) && vertex.length > 0);
    }
}