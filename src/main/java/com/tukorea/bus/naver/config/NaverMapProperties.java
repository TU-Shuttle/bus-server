package com.tukorea.bus.naver.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "naver.map.api")
public class NaverMapProperties {

    private String clientId;
    private String clientSecret;
    private String baseUrl = "https://maps.apigw.ntruss.com";
}

