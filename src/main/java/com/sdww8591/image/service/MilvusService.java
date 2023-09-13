package com.sdww8591.image.service;

import cn.hutool.json.JSONUtil;
import com.sdww8591.image.domain.CollectionField;
import com.sdww8591.image.domain.Image;
import com.sdww8591.image.domain.SearchResult;
import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.*;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.index.DescribeIndexParam;
import io.milvus.response.QueryResultsWrapper;
import io.milvus.response.SearchResultsWrapper;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MilvusService {

    @Value("${milvus.server}")
    private String serviceHost;

    @Value("${milvus.port}")
    private Integer servicePort;

    @Value("${milvus.collection}")
    private String collectionName;

    @Value("${milvus.nlist}")
    private Integer nlist;

    @Value("${milvus.nprobe}")
    private Integer nprobe;

    private MetricType metricType = MetricType.IP;

    private MilvusServiceClient serviceClient = null;

    @PostConstruct
    public void initMilvusServiceClient() {
        new ScheduledThreadPoolExecutor(1).scheduleWithFixedDelay(this::checkAndRefreshClient, 0, 1, TimeUnit.MINUTES);
    }

    @SneakyThrows
    public void insertImage2Milvus(Image image) {

        List<InsertParam.Field> multiVectors = new ArrayList<>();
        for (CollectionField collectionField: CollectionField.values()) {
            Object value = collectionField.getDomainField().get(image);
            if (Objects.nonNull(value)) {
                multiVectors.add(new InsertParam.Field(collectionField.getFieldType().getName(), Collections.singletonList(value)));
            }
        }

        if (CollectionUtils.isEmpty(multiVectors)) {
            log.warn("空白对象无法插入");
            return;
        }

        R<MutationResult> insertResult = serviceClient.insert(InsertParam.newBuilder()
                .withCollectionName(collectionName)
                .withFields(multiVectors)
                .build());
        if (insertResult.getStatus() == R.Status.Success.getCode()) {

            image.setId(insertResult.getData().getIDs().getIntId().getData(0));
            log.info("插入图片成功！ id：{}", insertResult.getData().getIDs().toString());
        }
    }

    public void flushIndex() {
        // 刷新索引
        R<FlushResponse> resp = serviceClient.flush(FlushParam.newBuilder()
                .addCollectionName(collectionName)
                .withSyncFlush(false)
                .build());

        if (Objects.equals(resp.getStatus(), R.Status.Success.getCode())) {
            log.info("刷新索引成功");
        }
    }

    @SneakyThrows
    public List<SearchResult> searchSemilarImage(Image image, int topk, int pageNo, int pageSize) {
        log.info("开始执行加载集合");
        localCollection();
        log.info("加载集合完成");

        int offset = (pageNo - 1) * pageSize;
        int limit = pageSize;

        R<SearchResults> searchResult = serviceClient.search(SearchParam.newBuilder()
                .withCollectionName(collectionName)
                // 设置返回最相似的图片数量
                .withTopK(topk)
                .withConsistencyLevel(ConsistencyLevelEnum.STRONG)
                .withMetricType(metricType)
                // 返回的字段信息
                .withOutFields(CollectionField.FIELD_NAME)
                // 设置向量字段的名称
                .withVectorFieldName("vector")
                .withVectors(Collections.singletonList(image.getVector()))
                // nprobe是指在搜索时需要遍历的最大倒排列表数，它的值越大，搜索速度越慢，但搜索精度越高
                // offset 偏移量，limit 每页查询数量，offset 从0开始
                .withParams(JSONUtil.createObj().set("nprobe", nprobe).set("offset", offset).set("limit", limit).toString())
                .build());

        if (searchResult.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException("请求异常，错误码:" + searchResult.getStatus());
        }

        SearchResultsWrapper resultsWrapper = new SearchResultsWrapper(searchResult.getData().getResults());
        if (!searchResult.getData().getResults().getIds().hasIntId()) {
            return Collections.emptyList();
        }

        List<SearchResult> resultList = resultsWrapper.getRowRecords().stream()
                .map(this::toResult)
                .collect(Collectors.toList());
        log.info("搜索完成");
        return resultList;
    }

    public void localCollection() {
        R<RpcStatus>loadResult = serviceClient.loadCollection(LoadCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withSyncLoad(true)
                .build());
        if (loadResult.getStatus() != R.Status.Success.getCode()){
            throw new RuntimeException("加载至内存失败，原因:" + loadResult.getMessage());
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
        CreateCollectionParam.Builder builder = CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName);

        for (CollectionField collectionField: CollectionField.values()) {
            builder.addFieldType(collectionField.getFieldType());
        }
        return builder.build();
    }

    public void deleteCollection() {
        DropCollectionParam dropCollectionParam = DropCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build();

        R<RpcStatus> resp = serviceClient.dropCollection(dropCollectionParam);
        if (resp.getStatus() == R.Status.Success.getCode()) {
            log.info("集合:{}删除成功", collectionName);
        }
    }

    public void checkCollectionIndex() {
        // 查询索引 返回0代表未创建索引需要创建索引
        R<DescribeIndexResponse> indexResult = serviceClient.describeIndex(DescribeIndexParam.newBuilder()
                .withCollectionName(collectionName).build());
        if (indexResult.getStatus() == R.Status.IndexNotExist.getCode()) {
            // 创建索引
            R<RpcStatus> resp = serviceClient.createIndex(createCollectionIndex(collectionName));
            if (resp.getStatus().equals(R.Status.Success.getCode())) {
                log.info("创建索引成功");
            }
        }
    }
    private CreateIndexParam createCollectionIndex(String collectionName) {
        return CreateIndexParam.newBuilder()
                .withCollectionName(collectionName)
                // 需要加索引的字段名称
                .withFieldName("vector")
                .withMetricType(metricType)
                .withSyncMode(Boolean.FALSE)
                .withIndexType(IndexType.IVF_FLAT)
                .withExtraParam(JSONUtil.createObj().set("nlist", nlist).toString())
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

    @SneakyThrows
    private SearchResult toResult(QueryResultsWrapper.RowRecord record) {
        if (Objects.isNull(record)) {
            return null;
        }

        Image domain = Image.builder().build();
        for (CollectionField field: CollectionField.values()) {
            Object val = record.get(field.getFieldType().getName());
            field.getDomainField().set(domain, val);
        }

        Float distance = (Float) record.get("distance");
        return new SearchResult(domain, distance);
    }
}
