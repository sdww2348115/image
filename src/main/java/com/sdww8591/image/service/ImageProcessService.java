package com.sdww8591.image.service;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Floats;
import com.sdww8591.image.domain.Image;
import com.sdww8591.image.util.FileUtils;
import jakarta.annotation.PostConstruct;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.graph.vertex.GraphVertex;
import org.deeplearning4j.zoo.ZooModel;
import org.deeplearning4j.zoo.model.ResNet50;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ImageProcessService {

    private ComputationGraph pretrainedNet;

    private String vertexName;

    @SneakyThrows
    @PostConstruct
    public void init() {
        log.info("开始初始化图形特征抽取模型");
        ZooModel<?> zooModel = ResNet50.builder().build();
        pretrainedNet = (ComputationGraph) zooModel.initPretrained();

        log.info("模型加载成功");

        GraphVertex vertex = pretrainedNet.getVertices()[pretrainedNet.getVertices().length - 1];
        vertexName = vertex.getVertexName();
        log.info("向量名赋值成功");
    }

    public Image buildImageFromFile(String path) {

        File file = new File(path);
        Preconditions.checkArgument(file.exists(), "文件不存在:" + path);

        return Image.builder()
                .name(file.getName())
                .path(file.getPath())
                .md5(FileUtils.calculateMD5(file))
                .vector(extractImageVector(file))
                .build();
    }

    @SneakyThrows
    public List<Float> extractImageVector(File img) {
        log.info("开始进行图片特征抽取");

        NativeImageLoader loader = new NativeImageLoader(224, 224, 3);
        // 加载文件到内存，生成INDArray
        INDArray image = loader.asMatrix(img);

        INDArray features = pretrainedNet.feedForward(image, false).get(vertexName);
        log.info("图片特征抽取成功");
        return Floats.asList(features.toFloatVector());
    }

}
