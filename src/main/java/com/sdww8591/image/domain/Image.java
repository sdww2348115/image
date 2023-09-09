package com.sdww8591.image.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class Image {

    private Long id;

    private String name;

    private String path;

    private String md5;

    private List<Float> vector;
}
