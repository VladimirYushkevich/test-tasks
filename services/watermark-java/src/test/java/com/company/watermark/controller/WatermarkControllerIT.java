package com.company.watermark.controller;

import org.junit.Test;

import static com.company.watermark.domain.Content.BOOK;
import static com.company.watermark.domain.Content.JOURNAL;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class WatermarkControllerIT extends BaseWatermarkControllerIT {

    @Test
    public void testWatermarkPublicationAsync_success() throws Exception {
        testWatermarkPublicationAsync_success(book, BOOK, notNullValue());
        testWatermarkPublicationAsync_success(journal, JOURNAL, nullValue());
    }

    @Test
    public void testWatermarkTicketStatusFlow_fail_updateNotAllowed() throws Exception {
        testWatermarkTicketStatusFlow_fail_updateNotAllowed(book, BOOK, notNullValue());
        testWatermarkTicketStatusFlow_fail_updateNotAllowed(journal, JOURNAL, nullValue());
    }

    @Test
    public void testWatermarkTicketStatusFlow_success_updateAllowedAfterSuccess() throws Exception {
        testWatermarkTicketStatusFlow_success_updateAllowedAfterSuccess(book, BOOK, notNullValue());
        testWatermarkTicketStatusFlow_success_updateAllowedAfterSuccess(journal, JOURNAL, nullValue());
    }

    @Test
    public void testWatermarkTicketStatusFlow_success_updateAllowedAfterFail() throws Exception {
        testWatermarkTicketStatusFlow_success_updateAllowedAfterFail(book, BOOK, notNullValue());
        testWatermarkTicketStatusFlow_success_updateAllowedAfterFail(journal, JOURNAL, nullValue());
    }
}
