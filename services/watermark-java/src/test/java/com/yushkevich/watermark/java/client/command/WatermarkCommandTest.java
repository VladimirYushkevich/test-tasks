package com.yushkevich.watermark.java.client.command;

import com.yushkevich.watermark.java.client.WatermarkClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;

@RunWith(MockitoJUnitRunner.class)
public class WatermarkCommandTest {

    @Mock
    private WatermarkClient watermarkClient;

    private WatermarkCommand watermarkCommand;

    @Before
    public void setUp() {
        watermarkCommand = new WatermarkCommand("WatermarkGroupTest", 1000, "testWatermarkDocument",
                Arrays.asList("A", "B", "C"), watermarkClient);
    }

    @Test
    public void testWatermarkDocument_success() throws Exception {
        delayWatermarkClient(500L, false);

        String watermarkProperty = watermarkCommand.observe()
                .toBlocking().toFuture().get();

        assertThat(watermarkProperty, is("watermarkTest"));
    }

    @Test
    public void testWatermarkDocument_clientTimeOut() throws Exception {
        delayWatermarkClient(1500L, false);

        String watermarkProperty = watermarkCommand.observe()
                .toBlocking().toFuture().get();

        assertThat(watermarkProperty, is(""));
    }

    @Test
    public void testWatermarkDocument_clientException() throws Exception {
        delayWatermarkClient(500L, true);

        String watermarkProperty = watermarkCommand.observe()
                .toBlocking().toFuture().get();

        assertThat(watermarkProperty, is(""));
    }

    private void delayWatermarkClient(long timeout, boolean isFailed) {
        doAnswer(invocation -> {
            Thread.sleep(timeout);
            if (isFailed) {
                throw new RuntimeException("Watermark client failed");
            }
            return "watermarkTest";
        }).when(watermarkClient).createWatermark(any());
    }

    @After
    public void tearDown() throws Exception {
        reset(watermarkClient);
        Thread.sleep(1000L);
    }
}
