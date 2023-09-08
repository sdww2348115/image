package com.sdww8591.image.service;

import com.sdww8591.image.ImageApplicationTests;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MilvusServiceTest {

    @Resource
    private MilvusService milvusService;

    @Test
    public void initCollection() {
        milvusService.initCollection();
    }
}