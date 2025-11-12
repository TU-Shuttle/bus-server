package com.tukorea.bus.common.response.handler;

import com.tukorea.bus.common.response.code.Code;
import com.tukorea.bus.common.response.dto.ErrorResponse;
import com.tukorea.bus.common.response.dto.SuccessResponse;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

public class ResponseHandler {

    public static ResponseEntity<SuccessResponse> success(Code code) {
        SuccessResponse response = new SuccessResponse(code, null);
        return ResponseEntity
                .status(code.getStatus())
                .body(response);
    }

    public static ResponseEntity<SuccessResponse> success(Code code, Object data) {
        SuccessResponse response = new SuccessResponse(code, data);
        return ResponseEntity
                .status(code.getStatus())
                .body(response);
    }

    public static ResponseEntity<ErrorResponse> error(Code code, WebRequest request) {
        ErrorResponse response = new ErrorResponse(code, request, null);
        return ResponseEntity
                .status(code.getStatus())
                .body(response);
    }

    public static ResponseEntity<ErrorResponse> error(Code code, WebRequest request,
                                                      Map<String, Object> additionalData) {
        ErrorResponse response = new ErrorResponse(code, request, additionalData);
        return ResponseEntity
                .status(code.getStatus())
                .body(response);
    }
}
