package com.company.watermark.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.List;

/**
 * Custom page class.
 */

@Data
@Builder
@Value
public class PageDTO<T> {
    private Integer totalPages;
    private Long totalEntries;
    private List<T> entries;
}