package com.tukorea.bus.schedule.controller;

import com.tukorea.bus.common.response.code.SuccessCode;
import com.tukorea.bus.common.response.dto.SuccessResponse;
import com.tukorea.bus.common.response.handler.ResponseHandler;
import com.tukorea.bus.schedule.service.ShuttleTimetableIngestService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/schedule")
@AllArgsConstructor
public class AdminScheduleController {

    private final ShuttleTimetableIngestService ingestService;

    @PostMapping("/refresh")
    @Operation(summary = "시간표 즉시 수집", description = "학교 페이지의 시간표를 지금 수집합니다. X-Admin-Token 헤더가 필요합니다.")
    public ResponseEntity<SuccessResponse> refresh() {
        return ResponseHandler.success(SuccessCode.UPDATE_SUCCESS, ingestService.refreshAll());
    }
}
