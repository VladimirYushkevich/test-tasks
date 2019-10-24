package com.yushkevich.watermark.controller;

import com.google.common.collect.Lists;
import com.yushkevich.watermark.dto.PublicationDTO;
import org.junit.Test;

import static com.yushkevich.watermark.domain.Content.BOOK;
import static com.yushkevich.watermark.domain.Content.JOURNAL;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class PublicationControllerIT extends BasePublicationControllerIT {

    @Test
    public void testCreatePublication_fail() throws Exception {
        testCreatePublication_fail(BOOK, Lists.newArrayList("title", "author", "topic"));
        testCreatePublication_fail(JOURNAL, Lists.newArrayList("title", "author"));
    }

    @Test
    public void testUpdatePublication_success() throws Exception {
        final PublicationDTO bookToUpdate = PublicationDTO.builder().content(BOOK).author("newAuthor").build();
        final PublicationDTO originalBook = resolvePublicationDTO(bookToUpdate.getContent());
        final Long bookId = createAndVerifyPublication(originalBook, is(book.getTopic().toString()));
        bookToUpdate.setId(bookId);
        testUpdatePublication_success(originalBook, bookToUpdate, is(book.getTopic().toString()));

        final PublicationDTO journalToUpdate = PublicationDTO.builder().content(JOURNAL).author("newAuthor").build();
        final PublicationDTO originalJournal = resolvePublicationDTO(journalToUpdate.getContent());
        final Long journalId = createAndVerifyPublication(originalJournal, nullValue());
        journalToUpdate.setId(journalId);
        testUpdatePublication_success(originalJournal, journalToUpdate, nullValue());
    }

    @Test
    public void testPublicationList_success() throws Exception {
        testPublicationList_success(BOOK, 2);
        testPublicationList_success(JOURNAL, 0);
    }

    @Test
    public void testFindPublication_success() throws Exception {
        testFindPublication_success(book, is(book.getTopic().toString()));
        testFindPublication_success(journal, nullValue());
    }

    @Test
    public void testFindPublication_fail() throws Exception {
        testFindPublication_fail(book, is(book.getTopic().toString()));
        testFindPublication_fail(journal, nullValue());
    }

    @Test
    public void testDeletePublication_success() throws Exception {
        testDeletePublication_success(book, is(book.getTopic().toString()));
        testDeletePublication_success(journal, nullValue());
    }

    @Test
    public void testDeletePublication_fail() throws Exception {
        testDeletePublication_fail(book, is(book.getTopic().toString()));
        testDeletePublication_fail(journal, nullValue());
    }
}
