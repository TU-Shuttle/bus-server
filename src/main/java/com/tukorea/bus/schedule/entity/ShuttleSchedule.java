package com.tukorea.bus.schedule.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 시간표의 한 줄. 정시 출발이면 {@code endTime}이 null, 수시운행 구간이면 {@code departTime}~{@code endTime}.
 */
@Entity
@Table(name = "shuttle_schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShuttleSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "timetable_id", nullable = false)
    private ShuttleTimetable timetable;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false, length = 10)
    private ShuttleDayType dayType;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 40)
    private ShuttleDirection direction;

    @Column(name = "depart_time", nullable = false)
    private LocalTime departTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "note", length = 200)
    private String note;

    @Builder
    private ShuttleSchedule(ShuttleTimetable timetable, ShuttleDayType dayType, ShuttleDirection direction,
                            LocalTime departTime, LocalTime endTime, String note) {
        this.timetable = timetable;
        this.dayType = dayType;
        this.direction = direction;
        this.departTime = departTime;
        this.endTime = endTime;
        this.note = note;
    }
}
