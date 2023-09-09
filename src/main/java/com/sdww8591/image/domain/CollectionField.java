package com.sdww8591.image.domain;

import com.sdww8591.image.util.ReflectionUtil;
import io.milvus.grpc.DataType;
import io.milvus.param.collection.FieldType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

@Getter
@AllArgsConstructor
public enum CollectionField {

    ID(FieldType.newBuilder()
            .withPrimaryKey(true)
            .withName("id")
            .withDataType(DataType.Int64)
            .withAutoID(true)
            .build(),
            ReflectionUtil.findFieldAndSetAccessible(Image.class, "id")),


    NAME(FieldType.newBuilder()
            .withName("name")
            .withDataType(DataType.VarChar)
            .withMaxLength(512)
            .build(),
            ReflectionUtil.findFieldAndSetAccessible(Image.class, "name")),


    PATH(FieldType.newBuilder()
            .withName("path")
            .withDataType(DataType.VarChar)
            .withMaxLength(2046)
            .build(),
            ReflectionUtil.findFieldAndSetAccessible(Image.class, "path")),

    MD5(FieldType.newBuilder()
            .withName("md5")
            .withDataType(DataType.VarChar)
            .withMaxLength(512)
            .build(),
            ReflectionUtil.findFieldAndSetAccessible(Image.class, "md5")),

    VECTOR(FieldType.newBuilder()
            .withName("vector")
            .withDataType(DataType.FloatVector)
            .withDimension(1000)
            .build(),
            ReflectionUtil.findFieldAndSetAccessible(Image.class, "vector"));

    private FieldType fieldType;
    private Field domainField;
}
