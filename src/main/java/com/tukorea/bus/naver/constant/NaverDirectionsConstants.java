package com.tukorea.bus.naver.constant;

public final class NaverDirectionsConstants {

    private NaverDirectionsConstants() {
        throw new AssertionError("인스턴스화할 수 없는 클래스입니다.");
    }

    public static final String API_PATH = "/map-direction/v1/driving";
    public static final String HEADER_CLIENT_ID = "X-NCP-APIGW-API-KEY-ID";
    public static final String HEADER_CLIENT_SECRET = "X-NCP-APIGW-API-KEY";
    public static final Integer SUCCESS_CODE = 0;
    public static final int FIRST_PATH_INDEX = 0;
}

