package com.tukorea.bus.common.response.code;

import org.springframework.http.HttpStatus;

public interface Code {

    HttpStatus getStatus();

    String getMessage();

    default String getErrorCode() {
        return null;
    }
}
