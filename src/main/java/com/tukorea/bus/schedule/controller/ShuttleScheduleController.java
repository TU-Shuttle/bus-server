package com.tukorea.bus.schedule.controller;

import com.tukorea.bus.common.response.code.SuccessCode;
import com.tukorea.bus.common.response.dto.SuccessResponse;
import com.tukorea.bus.common.response.handler.ResponseHandler;
import com.tukorea.bus.schedule.dto.FindShuttleScheduleResponse;
import com.tukorea.bus.schedule.service.ShuttleScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bus/schedule")
@AllArgsConstructor
public class ShuttleScheduleController {

    private final ShuttleScheduleService shuttleScheduleService;

    @GetMapping
    @Operation(summary = "버스 시간표 조회", description = "요일과 방향에 따른 셔틀버스 시간표를 조회합니다. 파라미터가 없으면 전체를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", ref = "Ok"),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "500", ref = "InternalServerError")
    })
    public ResponseEntity<SuccessResponse> findSchedules(
            @RequestParam(required = false) String dayType,
            @RequestParam(required = false) String direction
    ) {
        List<FindShuttleScheduleResponse> response = shuttleScheduleService.findSchedules(dayType, direction);
        return ResponseHandler.success(SuccessCode.RESOURCE_FOUND, response);
    }
}

