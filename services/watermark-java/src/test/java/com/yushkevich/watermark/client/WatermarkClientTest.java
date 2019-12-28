package com.yushkevich.watermark.client;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.hamcrest.number.OrderingComparison.*;
import static org.junit.Assert.assertThat;

public class WatermarkClientTest {

    private WatermarkClient watermarkClient = new WatermarkClient();

    private static final int WATERMARK_TIMEOUT = 1000;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(watermarkClient, "watermarkTimeOut", WATERMARK_TIMEOUT);
    }

    @Test
    public void testCreateWatermark() {
        final long start = System.currentTimeMillis();

        watermarkClient.createWatermark(Arrays.asList("book", "authorBook", "titleBook", "Science"));
        final int creationTime = (int) (System.currentTimeMillis() - start);

        assertThat(creationTime, greaterThanOrEqualTo(WATERMARK_TIMEOUT));
        assertThat(creationTime, lessThan(WATERMARK_TIMEOUT + 200));
    }
}
