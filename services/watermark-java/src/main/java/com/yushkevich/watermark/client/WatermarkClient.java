package com.yushkevich.watermark.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.yushkevich.watermark.utils.WatermarkGenerator.generateWatermark;

/**
 * Service simulating watermark generation. Assuming that all slow things happens here :). For simulation added delay property.
 */

@Service
@Slf4j
public class WatermarkClient {

    @Value("${watermark.client.delayInMilliseconds}")
    private int watermarkTimeOut;

    public String createWatermark(List<String> watermarkProperties) {
        log.debug("::STARTING watermark generation for {}", watermarkProperties);

        try {
            Thread.sleep(watermarkTimeOut);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

        final String watermark = generateWatermark(watermarkProperties);
        log.debug("::GENERATED watermark {}", watermark);
        return watermark;
    }
}
