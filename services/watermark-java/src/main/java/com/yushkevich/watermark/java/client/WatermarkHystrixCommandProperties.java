package com.yushkevich.watermark.java.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "hystrix.command.watermark")
@Getter
@Setter
public class WatermarkHystrixCommandProperties {
    private int timeoutInMilliseconds;
    private String groupKey;
}
