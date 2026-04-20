package com.huakai.indonesian.application.dto;

/**
 * 闪卡内容数据传输对象
 * 封装单词正面（印尼语）与背面（翻译、例句）的完整内容
 */
public record FlashCardDto(
    Long wordId,
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
