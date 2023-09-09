package com.sdww8591.image.application;

import com.sdww8591.image.TestBootStrap;
import com.sdww8591.image.util.FileUtils;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class ImageApplicationTest extends TestBootStrap {

    @Resource
    public ImageApplication imageApplication;

    @Test
    public void insertImage2Milvus() {

        String dataDirPath = "/Users/sdww/Downloads/ccx";

        FileUtils.traverseDirectory(new File(dataDirPath), imageApplication::insertImage2Milvus);
    }
}