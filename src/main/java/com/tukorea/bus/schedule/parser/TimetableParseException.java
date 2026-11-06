package com.tukorea.bus.schedule.parser;

/** 시간표 원본을 해석하지 못했을 때(원본 레이아웃 변경 등). 이 경우 기존 시간표를 유지한다. */
public class TimetableParseException extends RuntimeException {

    public TimetableParseException(String message) {
        super(message);
    }

    public TimetableParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
