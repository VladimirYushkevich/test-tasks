package com.yushkevich.watermark.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.yushkevich.watermark.domain.Content.Values.BOOK_VALUE;
import static com.yushkevich.watermark.domain.Content.Values.JOURNAL_VALUE;

@AllArgsConstructor
public enum Content {

    BOOK(BOOK_VALUE), JOURNAL(JOURNAL_VALUE);

    @Getter
    private String name;

    public static Content findByName(String name) {
        if (null == name) {
            return null;
        }

        for (Content content : values()) {
            if (content.getName().equals(name)) {
                return content;
            }
        }

        return null;
    }

    /**
     * Workaround to use enum as discriminator.
     */
    public static class Values {
        public static final String BOOK_VALUE = "book";
        public static final String JOURNAL_VALUE = "journal";
    }
}
