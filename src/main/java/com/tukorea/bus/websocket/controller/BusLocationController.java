package com.tukorea.bus.websocket.controller;

import com.tukorea.bus.common.response.code.BusinessErrorCode;
import com.tukorea.bus.common.response.code.SuccessCode;
import com.tukorea.bus.common.response.dto.SuccessResponse;
import com.tukorea.bus.common.response.exception.BusinessException;
import com.tukorea.bus.common.response.handler.ResponseHandler;
import com.tukorea.bus.websocket.dto.FindLatestBusLocationResponse;
import com.tukorea.bus.websocket.entity.BusLocation;
import com.tukorea.bus.websocket.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class BusLocationController {

    private final LocationService locationService;

    @GetMapping("/bus/{id}/location")
    @Operation(summary = "버스 최신 위치 조회", description = "특정 버스의 최신 위치 정보 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", ref = "Ok"),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "500", ref = "InternalServerError")
    })
    public ResponseEntity<SuccessResponse> findLatestBusLocation(@PathVariable String id) {
        BusLocation entity = locationService.findLatestBusLocation(id);
        if (entity == null) {
            throw new BusinessException(BusinessErrorCode.RESOURCE_NOT_FOUND);
        }

        return ResponseHandler.success(SuccessCode.RESOURCE_FOUND, new FindLatestBusLocationResponse(
                entity.getId(),
                entity.getLat(),
                entity.getLng(),
                entity.getAccuracy(),
                entity.getUpdatedAt()
        ));
    }
}
