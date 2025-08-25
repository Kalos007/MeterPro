package com.example.meter;

public class ZenoApiException extends RuntimeException {
    public ZenoApiException(String message) {
        super(message);
    }

    public ZenoApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
