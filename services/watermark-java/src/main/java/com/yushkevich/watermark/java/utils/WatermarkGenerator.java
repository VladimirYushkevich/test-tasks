package com.yushkevich.watermark.java.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Utility class to create watermark property (String).
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class WatermarkGenerator {

    public static String generateWatermark(List<String> watermarkProperties) {

        StringBuilder watermark = new StringBuilder();
        watermark.append("---*");
        watermarkProperties.forEach(wp -> watermark.append(wp).append("*"));
        watermark.append("---");

        return watermark.toString();
    }

}
