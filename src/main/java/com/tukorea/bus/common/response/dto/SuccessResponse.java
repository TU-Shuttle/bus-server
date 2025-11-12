package com.tukorea.bus.common.response.dto;

import com.tukorea.bus.common.response.code.Code;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SuccessResponse {

    private LocalDateTime timestamp;
    private int status;
    private String message;
    private Object data;

    public SuccessResponse(Code code, Object data) {
        this.timestamp = LocalDateTime.now();
        this.status = code.getStatus().value();
        this.message = code.getMessage();
        this.data = data;
    }
}