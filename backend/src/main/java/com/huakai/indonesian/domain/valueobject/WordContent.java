package com.huakai.indonesian.domain.valueobject;

import java.util.Objects;

/**
 * 单词内容值对象
 * 封装印尼语单词的显示内容：单词本身、翻译、例句
 * 不可变，由词书预置数据提供
 */
public final class WordContent {

    private final String indonesian;
    private final String chinese;
    private final String english;
    private final String exampleIndonesian;
    private final String exampleChinese;
    private final String exampleEnglish;

    /**
     * 创建单词内容值对象
     */
    public WordContent(String indonesian, String chinese, String english,
                       String exampleIndonesian, String exampleChinese, String exampleEnglish) {
        if (indonesian == null || indonesian.isBlank()) {
            throw new IllegalArgumentException("印尼语单词不能为空");
        }
        this.indonesian = indonesian;
        this.chinese = chinese;
        this.english = english;
        this.exampleIndonesian = exampleIndonesian;
        this.exampleChinese = exampleChinese;
        this.exampleEnglish = exampleEnglish;
    }

    public String indonesian() {
        return indonesian;
    }

    public String chinese() {
        return chinese;
    }

    public String english() {
        return english;
    }

    public String exampleIndonesian() {
        return exampleIndonesian;
    }

    public String exampleChinese() {
        return exampleChinese;
    }

    public String exampleEnglish() {
        return exampleEnglish;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WordContent)) return false;
        WordContent other = (WordContent) o;
        return Objects.equals(indonesian, other.indonesian);
    }

    @Override
    public int hashCode() {
        return Objects.hash(indonesian);
    }

    @Override
    public String toString() {
        return "WordContent{indonesian='" + indonesian + "'}";
    }
}
