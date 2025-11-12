package com.tukorea.bus.common.response.dto;

import com.tukorea.bus.common.response.code.Code;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.context.request.WebRequest;

@NoArgsConstructor
@Getter
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String code;
    private Map<String, Object> additionalData;

    public ErrorResponse(Code code, WebRequest request, Map<String, Object> additionalData) {
        this.timestamp = LocalDateTime.now();
        this.status = code.getStatus().value();
        this.error = code.getStatus().getReasonPhrase();
        this.message = code.getMessage();
        this.path = getPathFromRequest(request);
        this.code = code.getErrorCode();
        this.additionalData = additionalData;
    }

    private String getPathFromRequest(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}