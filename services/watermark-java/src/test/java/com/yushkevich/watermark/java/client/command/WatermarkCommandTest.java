package com.yushkevich.watermark.java.client.command;

import com.yushkevich.watermark.java.client.WatermarkClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WatermarkCommandTest {

    @Mock
    private WatermarkClient watermarkClient;

    private WatermarkCommand watermarkCommand;

    @Before
    public void setUp() {
        when(watermarkClient.createWatermark(any())).thenReturn("watermark");

        watermarkCommand = new WatermarkCommand("Tests", 2000, "watermarkDocument",
                Arrays.asList("A", "B", "C"), watermarkClient);
    }

    @Test
    @Ignore
    public void testWatermarkDocument_success() throws Exception {
        delayWatermarkClient(1000L);

        String watermarkProperty = watermarkCommand.observe()
                .toBlocking().toFuture().get();

        assertThat(watermarkProperty, is("watermark"));
    }

    @Test
    public void testWatermarkDocument_clientTimeOut() throws Exception {
        delayWatermarkClient(3000L);

        String watermarkProperty = watermarkCommand.observe()
                .toBlocking().toFuture().get();

        assertThat(watermarkProperty, is(""));
    }

    @Test
    @Ignore
    public void testWatermarkDocument_clientException() throws Exception {
        when(watermarkClient.createWatermark(any())).thenThrow(new RuntimeException("Watermark client failed"));

        String watermarkProperty = watermarkCommand.observe()
                .toBlocking().toFuture().get();

        assertThat(watermarkProperty, is(""));
    }

    private void delayWatermarkClient(long timeout) {
        doAnswer(invocation -> {
            Thread.sleep(timeout);
            return "watermark1";
        }).when(watermarkClient).createWatermark(any());
    }

    @After
    public void tearDown() throws Exception {
        reset(watermarkClient);
        Thread.sleep(1000L);
    }
}
