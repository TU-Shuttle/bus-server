package com.tukorea.bus.schedule.controller;

import com.tukorea.bus.common.response.code.SuccessCode;
import com.tukorea.bus.common.response.dto.SuccessResponse;
import com.tukorea.bus.common.response.handler.ResponseHandler;
import com.tukorea.bus.schedule.dto.ReplaceExtraTimetableRequest;
import com.tukorea.bus.schedule.service.ShuttleExtraScheduleService;
import com.tukorea.bus.schedule.service.ShuttleTimetableIngestService;
import jakarta.validation.Valid;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/schedule")
@AllArgsConstructor
public class AdminScheduleController {

    private final ShuttleTimetableIngestService ingestService;
    private final ShuttleExtraScheduleService extraScheduleService;

    @PostMapping("/refresh")
    @Operation(summary = "시간표 즉시 수집", description = "학교 페이지의 시간표를 지금 수집합니다. X-Admin-Token 헤더가 필요합니다.")
    public ResponseEntity<SuccessResponse> refresh() {
        return ResponseHandler.success(SuccessCode.UPDATE_SUCCESS, ingestService.refreshAll());
    }

    @PutMapping("/extra")
    @Operation(summary = "요일별 추가 운행 교체",
            description = "학사 공지 등으로 따로 나오는 요일별 운행(예: 조기취업학부 제2캠퍼스 셔틀)을 통째로 바꿉니다. "
                    + "학기가 바뀌면 시각만 고쳐 다시 올리면 됩니다. X-Admin-Token 헤더가 필요합니다.")
    public ResponseEntity<SuccessResponse> replaceExtra(@Valid @RequestBody ReplaceExtraTimetableRequest request) {
        return ResponseHandler.success(SuccessCode.UPDATE_SUCCESS, Map.of("rows", extraScheduleService.replace(request)));
    }
}
