package com.tukorea.bus.temp.controller;

import com.tukorea.bus.common.response.code.SuccessCode;
import com.tukorea.bus.common.response.dto.SuccessResponse;
import com.tukorea.bus.common.response.handler.ResponseHandler;
import com.tukorea.bus.temp.dto.FindTempResponse;
import com.tukorea.bus.temp.service.TempService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class TempController {

    private final TempService tempService;

    @GetMapping("/temps")
    @Operation(summary = "더미 데이터 다건 조회", description = "테스트용")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", ref = "Ok"),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "500", ref = "InternalServerError")
    })
    public ResponseEntity<SuccessResponse> getTemps() {
        List<FindTempResponse> response = tempService.findTemps();
        return ResponseHandler.success(SuccessCode.RESOURCE_FOUND, response);
    }
}
