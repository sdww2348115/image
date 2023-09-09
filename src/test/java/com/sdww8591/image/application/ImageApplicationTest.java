package com.sdww8591.image.application;

import com.sdww8591.image.TestBootStrap;
import com.sdww8591.image.domain.Image;
import com.sdww8591.image.domain.SearchResult;
import com.sdww8591.image.util.FileUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ImageApplicationTest extends TestBootStrap {

    @Resource
    public ImageApplication imageApplication;

    @Test
    public void insertImage2Milvus() {

        String dataDirPath = "/Users/sdww/Downloads/ccx";

        FileUtils.traverseDirectory(new File(dataDirPath), imageApplication::insertImage2Milvus);
    }

    @Test
    public void search() {

        String filePath = "/Users/sdww/Downloads/ccx/yoka_20200418193252.jpg";

        List<SearchResult> imageList = imageApplication.search(new File(filePath), 5, 1, 5);

        imageList.stream().sorted((o1, o2) -> Float.compare(o1.getDistance(), o2.getDistance()));
        Assertions.assertTrue(CollectionUtils.isNotEmpty(imageList));
    }
}