package com.tukorea.bus.naver.controller;

import com.tukorea.bus.common.response.code.SuccessCode;
import com.tukorea.bus.common.response.dto.SuccessResponse;
import com.tukorea.bus.common.response.handler.ResponseHandler;
import com.tukorea.bus.naver.dto.DirectionsRequest;
import com.tukorea.bus.naver.dto.NaverDirectionsResponse;
import com.tukorea.bus.naver.service.NaverDirectionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bus/directions")
@AllArgsConstructor
public class DirectionsController {

    private final NaverDirectionsService naverDirectionsService;

    @GetMapping("/{start}/{end}")
    @Operation(summary = "경로 탐색", description = "출발지와 목적지를 기반으로 자동차 경로를 탐색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", ref = "Ok"),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "500", ref = "InternalServerError")
    })
    public ResponseEntity<SuccessResponse> getDirections(
            @PathVariable @NotNull(message = "출발지 좌표는 필수입니다.") String start,
            @PathVariable @NotNull(message = "목적지 좌표는 필수입니다.") String end,
            @RequestParam(required = false) String waypoints,
            @RequestParam(required = false) String option,
            @RequestParam(required = false) Integer cartype
    ) {
        DirectionsRequest request = new DirectionsRequest(start, waypoints, end, option, cartype);
        NaverDirectionsResponse response = naverDirectionsService.getDirections(request);
        return ResponseHandler.success(SuccessCode.RESOURCE_FOUND, response);
    }
}

