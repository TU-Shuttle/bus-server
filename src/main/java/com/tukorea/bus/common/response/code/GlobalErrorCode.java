package com.tukorea.bus.common.response.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum GlobalErrorCode implements Code {

    UNEXPECTED(HttpStatus.INTERNAL_SERVER_ERROR, "예상하지 못한 에러가 발생하였습니다. 즉시 해결하세요.", "001"),

    METHOD_ARGUMENT_NOT_VALID(HttpStatus.BAD_REQUEST, "데이터 검증 에러가 발생하였습니다.", "002"),

    DATA_ACCESS(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 오류가 발생했습니다.", "003"),

    NO_RESOURCE_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다.", "004"),

    MESSAGING(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 요청을 처리하던 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.", "005"),

    ACCESS_DENIED(HttpStatus.FORBIDDEN, "요청한 리소스에 접근할 권한이 없습니다.", "006");

    private final HttpStatus status;
    private final String message;
    private final String errorCode;
}
