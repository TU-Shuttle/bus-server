package com.tukorea.bus.location.controller;

import com.tukorea.bus.common.response.code.BusinessErrorCode;
import com.tukorea.bus.common.response.exception.BusinessException;
import com.tukorea.bus.location.domain.BusLocation;
import com.tukorea.bus.location.dto.FindLatestBusLocationResponse;
import com.tukorea.bus.location.service.LocationService;
import com.tukorea.bus.location.stream.LocationStreamHub;
import io.swagger.v3.oas.annotations.Operation;
import com.tukorea.bus.location.websocket.DriverTokenVerifier;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/bus")
@AllArgsConstructor
public class BusLocationController {

    private final LocationService locationService;
    private final LocationStreamHub streamHub;
    private final DriverTokenVerifier tokenVerifier;

    @GetMapping("/{id}/location")
    @Operation(summary = "버스 최신 위치 조회", description = "운행 정보가 없거나 오래됐으면 404 입니다.")
    public FindLatestBusLocationResponse findLatestBusLocation(@PathVariable String id) {
        return locationService.findLatest(id)
                .map(FindLatestBusLocationResponse::from)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.RESOURCE_NOT_FOUND));
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "기사 앱: 이 번호를 쓸 수 있는지",
            description = "다른 기사가 이 번호로 운행 중이면 available=false. X-Driver-Token 이 필요합니다.")
    public Map<String, Boolean> checkAvailability(@PathVariable String id, @RequestParam String deviceId,
                                                  @RequestHeader(value = DriverTokenVerifier.HEADER, required = false)
                                                  String token) {
        if (!tokenVerifier.verify(token)) {
            throw new BusinessException(BusinessErrorCode.SESSION_INVALID);
        }
        return Map.of("available", locationService.isAvailable(id, deviceId));
    }

    @GetMapping("/locations")
    @Operation(summary = "운행 중인 모든 버스 위치 조회")
    public List<FindLatestBusLocationResponse> findAllLocations() {
        return locationService.findAllFresh().stream().map(FindLatestBusLocationResponse::from).toList();
    }

    @GetMapping(value = "/{id}/location/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "버스 위치 실시간 구독(SSE)", description = "연결 직후 현재 위치를 한 번 보내고, 이후 갱신마다 'location' 이벤트를 보냅니다.")
    public SseEmitter streamLocation(@PathVariable String id) {
        List<BusLocation> initial = locationService.findLatest(id).map(List::of).orElse(List.of());
        return streamHub.subscribe(id, initial);
    }

    @GetMapping(value = "/location/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "모든 버스 위치 실시간 구독(SSE)")
    public SseEmitter streamAllLocations() {
        return streamHub.subscribe(null, locationService.findAllFresh());
    }
}
