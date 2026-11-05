package com.tukorea.bus.ride.controller;

import com.tukorea.bus.common.response.code.SuccessCode;
import com.tukorea.bus.common.response.dto.SuccessResponse;
import com.tukorea.bus.common.response.handler.ResponseHandler;
import com.tukorea.bus.ride.dto.CreateRideRequest;
import com.tukorea.bus.ride.service.RideIntentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 탑승 예정(수요 파악). 본인 확인은 앱이 만든 익명 기기 id(X-Device-Id)로 한다. */
@RestController
@RequestMapping("/api/rides")
@AllArgsConstructor
public class RideIntentController {

    public static final String DEVICE_HEADER = "X-Device-Id";

    private final RideIntentService rideIntentService;

    @PostMapping
    @Operation(summary = "탑승 예정 등록", description = "같은 출발편에 다시 보내면 하차지만 바뀝니다.")
    public ResponseEntity<SuccessResponse> reserve(@RequestHeader(DEVICE_HEADER) String deviceId,
                                                   @Valid @RequestBody CreateRideRequest request) {
        return ResponseHandler.success(SuccessCode.REGISTER_SUCCESS, rideIntentService.reserve(deviceId, request));
    }

    @GetMapping("/mine")
    @Operation(summary = "내 탑승 예정(오늘 이후)")
    public ResponseEntity<SuccessResponse> findMine(@RequestHeader(DEVICE_HEADER) String deviceId) {
        return ResponseHandler.success(SuccessCode.RESOURCE_FOUND, rideIntentService.findMine(deviceId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "탑승 예정 취소")
    public ResponseEntity<SuccessResponse> cancel(@RequestHeader(DEVICE_HEADER) String deviceId,
                                                  @PathVariable Long id) {
        rideIntentService.cancel(deviceId, id);
        return ResponseHandler.success(SuccessCode.DELETE_SUCCESS);
    }

    @GetMapping("/summary")
    @Operation(summary = "날짜별 출발편 탑승 예정 인원", description = "기사 앱과 학생 앱이 '몇 명 타요'를 보여줄 때 씁니다.")
    public ResponseEntity<SuccessResponse> summarize(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseHandler.success(SuccessCode.RESOURCE_FOUND, rideIntentService.summarize(date));
    }
}
