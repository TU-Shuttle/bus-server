package com.tukorea.bus.ride.entity;

import com.tukorea.bus.schedule.entity.ShuttleDirection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * "이 셔틀 탈게요" 한 건. 수요(몇 명이 어디서 내리는지) 파악용이며 좌석을 보장하지 않는다.
 * 출발편은 시간표 행 id 대신 (날짜, 방향, 출발 시각)으로 가리켜 시간표를 다시 수집해도 유지된다.
 * 로그인 없이 앱이 만든 익명 기기 id로 본인 것을 구분한다.
 */
@Entity
@Table(name = "ride_intent",
        uniqueConstraints = @UniqueConstraint(name = "uk_ride_intent_device_departure",
                columnNames = {"device_id", "service_date", "direction", "depart_time"}),
        indexes = @Index(name = "idx_ride_intent_date", columnList = "service_date"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RideIntent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false, length = 64)
    private String deviceId;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 40)
    private ShuttleDirection direction;

    @Column(name = "depart_time", nullable = false)
    private LocalTime departTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "drop_off", nullable = false, length = 30)
    private ShuttleStop dropOff;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    private RideIntent(String deviceId, LocalDate serviceDate, ShuttleDirection direction, LocalTime departTime,
                       ShuttleStop dropOff, LocalDateTime createdAt) {
        this.deviceId = deviceId;
        this.serviceDate = serviceDate;
        this.direction = direction;
        this.departTime = departTime;
        this.dropOff = dropOff;
        this.createdAt = createdAt;
    }

    public void changeDropOff(ShuttleStop dropOff) {
        this.dropOff = dropOff;
    }
}
