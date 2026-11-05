package com.tukorea.bus.location.service;

/** 기사 앱이 보낸 위치 메시지가 올바르지 않을 때. */
public class InvalidLocationException extends RuntimeException {

    public InvalidLocationException(String message) {
        super(message);
    }
}
