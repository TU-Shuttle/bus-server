package com.tukorea.bus.schedule.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 학교 셔틀 페이지 밖(학사 공지 등)에서 따로 나오는 요일별 운행. 예: 조기취업학부 제2캠퍼스 셔틀.
 * 경로(방향)는 고정이고 학기마다 시각만 바뀌므로, 관리자 API로 통째로 교체한다.
 */
@Entity
@Table(name = "shuttle_extra_schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShuttleExtraSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 40)
    private ShuttleDirection direction;

    @Column(name = "depart_time", nullable = false)
    private LocalTime departTime;

    /** "MON,WED" 처럼 쉼표로 구분한 요일. */
    @Column(name = "days_of_week", nullable = false, length = 40)
    private String daysOfWeek;

    @Column(name = "note", length = 200)
    private String note;

    @Builder
    private ShuttleExtraSchedule(String title, LocalDate validFrom, LocalDate validTo, ShuttleDirection direction,
                                 LocalTime departTime, List<DayOfWeek> daysOfWeek, String note) {
        this.title = title;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.direction = direction;
        this.departTime = departTime;
        this.daysOfWeek = daysOfWeek.stream().sorted().map(DayOfWeek::name).collect(Collectors.joining(","));
        this.note = note;
    }

    public List<DayOfWeek> days() {
        return Arrays.stream(daysOfWeek.split(",")).map(String::trim).map(DayOfWeek::valueOf).sorted().toList();
    }

    /** 토요일만 운행하면 토요일 시간표, 아니면 평일 시간표에 넣는다. */
    public ShuttleDayType dayType() {
        return days().stream().allMatch(d -> d == DayOfWeek.SATURDAY) ? ShuttleDayType.SATURDAY : ShuttleDayType.WEEKDAY;
    }

    public boolean isActiveOn(LocalDate date) {
        return validTo == null || !date.isAfter(validTo);
    }
}
