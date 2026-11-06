package com.tukorea.bus.schedule.controller;

import com.tukorea.bus.common.response.code.SuccessCode;
import com.tukorea.bus.common.response.dto.SuccessResponse;
import com.tukorea.bus.common.response.handler.ResponseHandler;
import com.tukorea.bus.schedule.service.ShuttleScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bus/timetable")
@AllArgsConstructor
public class ShuttleTimetableController {

    private final ShuttleScheduleService shuttleScheduleService;

    @GetMapping
    @Operation(summary = "현재 시간표 전체 조회", description = "제목·적용 기간·수집 시각과 모든 출발 시각을 한 번에 돌려줍니다. 활성 시간표가 없으면 404 입니다.")
    public ResponseEntity<SuccessResponse> findActiveTimetable() {
        return ResponseHandler.success(SuccessCode.RESOURCE_FOUND, shuttleScheduleService.findActiveTimetable());
    }
}
