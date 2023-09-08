package com.sdww8591.image.service;

import com.sdww8591.image.ImageApplicationTests;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class ImageProcessServiceTest extends ImageApplicationTests {

    @Resource
    private ImageProcessService imageProcessService;

    @Test
    public void extractImageVector() {
        float[] vertex = imageProcessService.extractImageVector("C:\\Users\\sdww\\IdeaProjects\\milvus\\IMG_20220410_174509_1.jpg");
        Assertions.assertTrue(Objects.nonNull(vertex) && vertex.length > 0);
    }
}