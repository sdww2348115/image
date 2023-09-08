package com.sdww8591.image.service;

import cn.hutool.json.JSONUtil;
import com.google.common.primitives.Floats;
import com.sdww8591.image.domain.Image;
import io.milvus.client.MilvusServiceClient;
import io.milvus.common.utils.JacksonUtils;
import io.milvus.grpc.CheckHealthResponse;
import io.milvus.grpc.DataType;
import io.milvus.grpc.DescribeIndexResponse;
import io.milvus.grpc.MutationResult;
import io.milvus.param.*;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.index.DescribeIndexParam;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.json.Json;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MilvusService {

    @Value("${milvus.server}")
    private String serviceHost;

    @Value("${milvus.port}")
    private Integer servicePort;

    @Value("${milvus.collection}")
    private String collectionName;

    private MilvusServiceClient serviceClient = null;

    @PostConstruct
    public void initMilvusServiceClient() {
        new ScheduledThreadPoolExecutor(1).scheduleWithFixedDelay(this::checkAndRefreshClient, 0, 1, TimeUnit.MINUTES);
    }

    public void insertImage2Milvus(Image image) {

        List<InsertParam.Field> multiVectors = new ArrayList<>();
        multiVectors.add(new InsertParam.Field("name", Collections.singletonList(image.getName())));
        multiVectors.add(new InsertParam.Field("path", Collections.singletonList(image.getPath())));
        multiVectors.add(new InsertParam.Field("vector", Collections.singletonList(Floats.asList(image.getVector()))));

        R<MutationResult> insertResult = serviceClient.insert(InsertParam.newBuilder()
                .withCollectionName(collectionName)
                .withFields(multiVectors)
                .build());
        if (insertResult.getStatus() == R.Status.Success.getCode()) {

            image.setId(insertResult.getData().getIDs().getIntId().getData(0));
            log.info("插入图片成功！ id：{}", insertResult.getData().getIDs().toString());
        }
    }

    /**
     * 创建集合
     */
    public void initCollection() {
        // 获取集合
        R<Boolean> collection = serviceClient.hasCollection(HasCollectionParam.newBuilder()
                .withCollectionName(collectionName).build());
        if (!collection.getData()) {
            // 创建集合
            R<RpcStatus> response = serviceClient.createCollection(createCollection(collectionName));
            log.info("创建集合成功， resp:{}", response.getData().getMsg());
        }
    }

    /**
     * 以下为自定义字段，必须存在一个FloatVector类型字段，必须设置主键，没有可以用自增
     */
    private CreateCollectionParam createCollection(String collectionName){
        return CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                // id 主键 必须有一个主键，也可以自动生成主键使用withAutoID(true)
                .addFieldType(FieldType.newBuilder()
                        .withPrimaryKey(true)
                        .withName("id")
                        .withDataType(DataType.Int64)
                        .withAutoID(true)
                        .build())
                .addFieldType(FieldType.newBuilder()
                        .withName("name")
                        .withDataType(DataType.VarChar)
                        .withMaxLength(512)
                        .build())
                .addFieldType(FieldType.newBuilder()
                        .withName("path")
                        .withDataType(DataType.VarChar)
                        .withMaxLength(2046)
                        .build())
                .addFieldType(FieldType.newBuilder()
                        .withName("vector")
                        .withDataType(DataType.FloatVector)
                        .withDimension(1000)
                        .build())
                .build();
    }

    public void checkCollectionIndex() {
        // 查询索引 返回0代表未创建索引需要创建索引
        R<DescribeIndexResponse> indexResult = serviceClient.describeIndex(DescribeIndexParam.newBuilder()
                .withCollectionName(collectionName).build());
        if (indexResult.getStatus() == R.Status.IndexNotExist.getCode()) {
            // 创建索引
            R<RpcStatus> resp = serviceClient.createIndex(createCollectionIndex(collectionName));
            log.info("创建索引:{}", resp.getMessage());
        }
    }
    private CreateIndexParam createCollectionIndex(String collectionName) {
        return CreateIndexParam.newBuilder()
                .withCollectionName(collectionName)
                // 需要加索引的字段名称
                .withFieldName("vector")
                .withMetricType(MetricType.IP)
                .withSyncMode(Boolean.FALSE)
                .withIndexType(IndexType.IVF_FLAT)
                .withExtraParam(JSONUtil.createObj().set("nlist", "1024").toString())
                .build();
    }

    private void checkAndRefreshClient() {
        log.info("检查milvus连接状态...");
        if (Objects.isNull(serviceClient) || !checkClientHealth(serviceClient)) {
            initClient();
        }
        log.info("milvus连接状态检查完成");
    }

    private Boolean checkClientHealth(MilvusServiceClient client) {
        R<CheckHealthResponse> response = client.checkHealth();
        if (!Objects.equals(R.Status.Success.getCode(), response.getStatus())) {
            return false;
        }
        if (response.getData().getIsHealthy()) {
            return true;
        }
        return false;
    }

    private void initClient() {
        serviceClient = new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withHost(serviceHost)
                        .withPort(servicePort)
                        .build()
        );

        log.info("milvus client inited!");
    }

}
