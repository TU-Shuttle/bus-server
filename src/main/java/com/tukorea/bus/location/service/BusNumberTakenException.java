package com.tukorea.bus.location.service;

/** 다른 기사(다른 기기)가 지금 운행 중인 버스 id로 위치를 올렸을 때. */
public class BusNumberTakenException extends InvalidLocationException {

    private final String busId;

    public BusNumberTakenException(String busId) {
        super("다른 기사가 이 번호로 운행 중입니다.");
        this.busId = busId;
    }

    public String busId() {
        return busId;
    }
}
