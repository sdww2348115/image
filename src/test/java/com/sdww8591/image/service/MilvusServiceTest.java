package com.sdww8591.image.service;

import com.sdww8591.image.util.FileUtils;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest
public class MilvusServiceTest {

    @Resource
    private MilvusService milvusService;

    @Test
    public void initCollection() {
        milvusService.initCollection();
    }

    @Test
    public void initIndex() {
        milvusService.checkCollectionIndex();
    }

    @Test
    public void deleteCollection() {
        milvusService.deleteCollection();
    }

    @Test
    public void flushIndex() {
        milvusService.flushIndex();
    }

    @Test
    public void initAll() {
        deleteCollection();
        initCollection();
        initIndex();
    }
}