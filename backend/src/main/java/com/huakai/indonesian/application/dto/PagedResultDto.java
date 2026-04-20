package com.huakai.indonesian.application.dto;

import java.util.List;

/**
 * 通用分页结果数据传输对象
 * 避免用 Map 封装分页数据
 */
public record PagedResultDto<T>(
    long total,
    int page,
    int size,
    List<T> items
) {
}
