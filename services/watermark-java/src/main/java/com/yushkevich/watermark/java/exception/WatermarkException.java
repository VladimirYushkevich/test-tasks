package com.yushkevich.watermark.java.exception;


import java.io.Serializable;

public class WatermarkException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 8825355620424918338L;

    public WatermarkException(String message) {
        super(message);
    }
}
