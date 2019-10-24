package com.yushkevich.watermark.repository;

import org.junit.Test;

import static com.yushkevich.watermark.domain.Content.BOOK;
import static com.yushkevich.watermark.domain.Content.JOURNAL;

public class PublicationRepositoryIT extends BasePublicationRepositoryIT {

    @Test
    public void testPublicationCrudOperations() {
        testPublicationCrudOperations(BOOK);
        testPublicationCrudOperations(JOURNAL);
    }

    @Test
    public void testPublicationPageable() throws Exception {
        testPublicationPageable(BOOK);
        testPublicationPageable(JOURNAL);
    }
}
