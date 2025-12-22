package com.tukorea.bus.naver.service;

import static com.tukorea.bus.naver.constant.NaverDirectionsConstants.API_PATH;
import static com.tukorea.bus.naver.constant.NaverDirectionsConstants.HEADER_CLIENT_ID;
import static com.tukorea.bus.naver.constant.NaverDirectionsConstants.HEADER_CLIENT_SECRET;
import static com.tukorea.bus.naver.constant.NaverDirectionsConstants.SUCCESS_CODE;

import com.tukorea.bus.common.response.code.BusinessErrorCode;
import com.tukorea.bus.common.response.exception.BusinessException;
import com.tukorea.bus.naver.config.NaverMapProperties;
import com.tukorea.bus.naver.dto.DirectionsRequest;
import com.tukorea.bus.naver.dto.NaverDirectionsResponse;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@AllArgsConstructor
public class NaverDirectionsService {

    private final NaverMapProperties naverMapProperties;
    private final WebClient.Builder webClientBuilder;

    public NaverDirectionsResponse getDirections(DirectionsRequest request) {
        log.debug("네이버 Directions API 호출: start={}, end={}", request.start(), request.end());

        try {
            NaverDirectionsResponse naverResponse = callNaverDirectionsApi(request);
            validateResponse(naverResponse);
            return naverResponse;
            
        } catch (WebClientResponseException e) {
            log.error("네이버 Directions API HTTP 오류: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new BusinessException(BusinessErrorCode.EXTERNAL_API_ERROR);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("네이버 Directions API 호출 중 예외 발생", e);
            throw new BusinessException(BusinessErrorCode.EXTERNAL_API_ERROR);
        }
    }

    private NaverDirectionsResponse callNaverDirectionsApi(DirectionsRequest request) {
        validateApiKeys();
        
        WebClient webClient = buildWebClient();
        return executeApiCall(webClient, request);
    }

    private void validateApiKeys() {
        String clientId = naverMapProperties.getClientId();
        String clientSecret = naverMapProperties.getClientSecret();
        
        if (clientId == null || clientId.isEmpty() || clientSecret == null || clientSecret.isEmpty()) {
            log.error("네이버 API 키가 설정되지 않았습니다. clientId: {}, clientSecret: {}", 
                    clientId != null ? "설정됨" : "null", 
                    clientSecret != null ? "설정됨" : "null");
            throw new BusinessException(BusinessErrorCode.EXTERNAL_API_ERROR);
        }
        
        log.debug("네이버 API 호출 - clientId 길이: {}, clientSecret 길이: {}", 
                clientId.length(), clientSecret.length());
    }

    private WebClient buildWebClient() {
        return webClientBuilder
                .baseUrl(naverMapProperties.getBaseUrl())
                .build();
    }

    private NaverDirectionsResponse executeApiCall(WebClient webClient, DirectionsRequest request) {
        String clientId = naverMapProperties.getClientId();
        String clientSecret = naverMapProperties.getClientSecret();
        
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(API_PATH)
                        .queryParam("start", request.start())
                        .queryParam("goal", request.end())
                        .queryParamIfPresent("waypoints", Optional.ofNullable(request.waypoints()))
                        .queryParamIfPresent("option", Optional.ofNullable(request.option()))
                        .queryParamIfPresent("cartype", Optional.ofNullable(request.cartype()))
                        .build())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HEADER_CLIENT_ID, clientId)
                .header(HEADER_CLIENT_SECRET, clientSecret)
                .retrieve()
                .bodyToMono(NaverDirectionsResponse.class)
                .doOnError(error -> log.error("네이버 Directions API 호출 실패", error))
                .block();
    }

    private void validateResponse(NaverDirectionsResponse naverResponse) {
        if (naverResponse == null) {
            log.error("네이버 Directions API 응답이 null입니다.");
            throw new BusinessException(BusinessErrorCode.EXTERNAL_API_ERROR);
        }

        if (!SUCCESS_CODE.equals(naverResponse.getCode()) && naverResponse.getRoute() == null) {
            log.error("네이버 Directions API 오류: code={}, message={}", naverResponse.getCode(), naverResponse.getMessage());
            throw new BusinessException(BusinessErrorCode.EXTERNAL_API_ERROR);
        }
    }
}

