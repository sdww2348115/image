package com.sdww8591.image.service;

import jakarta.annotation.PostConstruct;
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

    @SneakyThrows
    public float[] extractImageVector(String path) {
        NativeImageLoader loader = new NativeImageLoader(224, 224, 3);
        // 加载文件到内存，生成INDArray
        File img = new File(path);
        INDArray image = loader.asMatrix(img);

        INDArray features = pretrainedNet.feedForward(image, false).get(vertexName);
        return features.toFloatVector();
    }

}
