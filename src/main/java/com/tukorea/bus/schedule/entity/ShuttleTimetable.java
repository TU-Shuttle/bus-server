package com.tukorea.bus.schedule.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 학교 홈페이지에서 수집한 시간표 한 벌(한 번의 수집 결과). 같은 원본({@code sourceUrl})에서는 하나만 active 이다.
 */
@Entity
@Table(name = "shuttle_timetable")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShuttleTimetable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    /** 시간표 적용 시작일. 원본에서 읽지 못하면 null. */
    @Column(name = "valid_from")
    private LocalDate validFrom;

    /** 시간표 적용 종료일. 원본에서 읽지 못하면 null. */
    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "source_url", nullable = false, length = 300)
    private String sourceUrl;

    /** 원본 파일의 SHA-256(hex). 같은 해시면 다시 반영하지 않는다. */
    @Column(name = "source_hash", nullable = false, length = 64)
    private String sourceHash;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Builder
    private ShuttleTimetable(String title, LocalDate validFrom, LocalDate validTo, String sourceUrl,
                             String sourceHash, LocalDateTime fetchedAt, boolean active) {
        this.title = title;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.sourceUrl = sourceUrl;
        this.sourceHash = sourceHash;
        this.fetchedAt = fetchedAt;
        this.active = active;
    }

    public void deactivate() {
        this.active = false;
    }
}
