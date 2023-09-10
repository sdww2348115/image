package com.sdww8591.image.job;

import com.sdww8591.image.TestBootStrap;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

public class SyncPhotosJobTest extends TestBootStrap {

    @Resource
    private SyncPhotosJob syncPhotosJob;

    @Test
    public void test() {
        syncPhotosJob.syncPhotos("G:\\data\\testImgs", "G:\\data\\target", "G:\\data\\conflict");
    }

}