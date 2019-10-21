package com.yushkevich.watermark.java;

import com.yushkevich.watermark.java.domain.*;

import static com.yushkevich.watermark.java.domain.Book.Topic.SCIENCE;
import static com.yushkevich.watermark.java.domain.Watermark.Status.NEW;

/**
 * Simple factory to createOrUpdate data for testing.
 */
public class RepositoryDataFactory {

    public static Book createBook() {
        final Book book = Book.builder()
                .title("titleBook")
                .author("authorBook")
                .topic(SCIENCE)
                .build();

        book.setWatermark(Watermark.builder()
                .publication(book)
                .status(NEW.getName())
                .build());

        return book;
    }

    public static Journal createJournal() {
        final Journal journal = Journal.builder()
                .title("titleJournal")
                .author("authorJournal")
                .build();

        journal.setWatermark(Watermark.builder()
                .publication(journal)
                .status(NEW.getName())
                .build());

        return journal;
    }

    public static Publication buildPublication(Content content) {
        switch (content) {
            case BOOK:
                return createBook();
            case JOURNAL:
                return createJournal();
            default:
                throw new RuntimeException();
        }
    }
}
