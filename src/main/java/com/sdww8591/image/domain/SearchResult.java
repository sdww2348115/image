package com.sdww8591.image.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchResult {

    private Image image;

    private Float distance;
}
