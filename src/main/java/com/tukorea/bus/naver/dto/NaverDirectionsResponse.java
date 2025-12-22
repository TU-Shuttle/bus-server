package com.tukorea.bus.naver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverDirectionsResponse {
    
    private Integer code;
    private String message;
    private String currentDateTime;
    private Route route;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Route {
        @JsonProperty("trafast")
        private List<Path> trafast;
        
        @JsonProperty("traoptimal")
        private List<Path> traoptimal;
        
        @JsonProperty("tracomfort")
        private List<Path> tracomfort;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Path {
        private Summary summary;
        private List<List<Double>> path;
        private List<Section> section;
        private List<Guide> guide;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Guide {
        private Integer pointIndex;
        private Integer type;
        private String instructions;
        private Integer distance;
        private Integer duration;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Summary {
        private Point start;
        private Point goal;
        private Integer distance;
        private Integer duration;
        private String departureTime;
        private List<List<Double>> bbox;
        private Integer tollFare;
        private Integer taxiFare;
        private Integer fuelPrice;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Point {
        private List<Double> location;
        private Integer dir;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Section {
        private Integer pointIndex;
        private Integer pointCount;
        private Integer distance;
        private String name;
        private Integer congestion;
        private Integer speed;
    }
    
}

