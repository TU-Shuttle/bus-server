package com.tukorea.bus.common.response.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum BusinessErrorCode implements Code {

    // 401 권한 부족
    SESSION_INVALID(HttpStatus.UNAUTHORIZED, "세션에 정보가 없습니다.", "682"),

    // 404 잘못된 리소스 접근
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 정보입니다.", "173"),

    // 400 데이터 저장 오류
    SAVE_FAILED(HttpStatus.BAD_REQUEST, "이미 존재하는 정보입니다.", "096"),

    // 500 데이터 수정 오류
    UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "데이터 수정을 실패했습니다.", "049"),

    // 500 데이터 삭제 오류
    DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "데이터 삭제를 실패했습니다.", "079");

    private final HttpStatus status;
    private final String message;
    private final String errorCode;
}
