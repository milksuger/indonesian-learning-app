package com.huakai.indonesian.application.dto;

/**
 * 单词搜索结果数据传输对象
 * 包含单词内容与所属词书信息
 */
public record WordSearchResultDto(
    Long wordId,
    Long wordbookId,
    String wordbookName,
    String indonesian,
    String chinese,
    String english,
    String exampleIndonesian,
    String exampleChinese,
    String exampleEnglish,
    boolean isFavorited,
    boolean isMemorized
) {
}
