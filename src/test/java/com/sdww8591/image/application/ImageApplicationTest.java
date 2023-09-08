package com.sdww8591.image.application;

import com.sdww8591.image.TestBootStrap;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class ImageApplicationTest extends TestBootStrap {

    @Resource
    public ImageApplication imageApplication;

    @Test
    public void insertImage2Milvus() {
        imageApplication.insertImage2Milvus("C:\\Users\\sdww\\IdeaProjects\\milvus\\IMG_20220410_174509_1.jpg");
    }
}