package com.sdww8591.image.service;

import io.milvus.client.MilvusServiceClient;
import io.milvus.common.utils.JacksonUtils;
import io.milvus.grpc.CheckHealthResponse;
import io.milvus.grpc.DataType;
import io.milvus.param.ConnectParam;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    private void checkAndRefreshClient() {
        if (Objects.isNull(serviceClient) || !checkClientHealth(serviceClient)) {
            initClient();
        }
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
