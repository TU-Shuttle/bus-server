package com.tukorea.bus.schedule.service.impl;

import com.tukorea.bus.schedule.client.TimetableDownloader;
import com.tukorea.bus.schedule.client.TimetableDownloader.DownloadedFile;
import com.tukorea.bus.schedule.config.ShuttleTimetableProperties;
import com.tukorea.bus.schedule.entity.ShuttleSchedule;
import com.tukorea.bus.schedule.entity.ShuttleTimetable;
import com.tukorea.bus.schedule.parser.ParsedTimetable;
import com.tukorea.bus.schedule.parser.ShuttleTimetableParser;
import com.tukorea.bus.schedule.repository.ShuttleScheduleRepository;
import com.tukorea.bus.schedule.repository.ShuttleTimetableRepository;
import com.tukorea.bus.schedule.service.ShuttleTimetableIngestService;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 다운로드 → 해시 비교 → 파싱 → (트랜잭션) 교체 순서로 처리한다.
 * 파싱/다운로드가 실패하면 DB는 건드리지 않으므로 기존 시간표가 그대로 유지된다.
 */
@Slf4j
@Service
public class ShuttleTimetableIngestServiceImpl implements ShuttleTimetableIngestService {

    /** 파서 출력이 바뀔 때마다 올린다. 2: 제2캠퍼스 17:40 오이도역행 분리. */
    private static final String PARSER_VERSION = "2";

    private static final Pattern YEAR_IN_FILE_NAME = Pattern.compile("(\\d{2})년");

    private final ShuttleTimetableProperties properties;
    private final TimetableDownloader downloader;
    private final ShuttleTimetableParser parser;
    private final ShuttleTimetableRepository timetableRepository;
    private final ShuttleScheduleRepository scheduleRepository;
    private final TransactionTemplate transactionTemplate;

    public ShuttleTimetableIngestServiceImpl(ShuttleTimetableProperties properties,
                                             TimetableDownloader downloader,
                                             ShuttleTimetableParser parser,
                                             ShuttleTimetableRepository timetableRepository,
                                             ShuttleScheduleRepository scheduleRepository,
                                             PlatformTransactionManager transactionManager) {
        this.properties = properties;
        this.downloader = downloader;
        this.parser = parser;
        this.timetableRepository = timetableRepository;
        this.scheduleRepository = scheduleRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /** 스케줄러와 수동 트리거가 겹쳐 같은 원본을 동시에 반영하지 않도록 직렬화한다. */
    @Override
    public synchronized List<RefreshResult> refreshAll() {
        return properties.sourceUrls().stream().distinct().map(this::refresh).toList();
    }

    private RefreshResult refresh(String sourceUrl) {
        try {
            DownloadedFile file = downloader.download(sourceUrl);
            String hash = sha256(file.content());

            if (timetableRepository.findFirstBySourceHashAndActiveTrue(hash).isPresent()) {
                log.info("시간표 변경 없음: {}", sourceUrl);
                return new RefreshResult(sourceUrl, Status.UNCHANGED, "변경 없음");
            }

            ParsedTimetable parsed = parser.parse(file.content(), yearOf(file.fileName()));
            Long timetableId = transactionTemplate.execute(status -> replace(sourceUrl, hash, parsed));
            log.info("시간표 갱신: {} (id={}, {}건)", sourceUrl, timetableId, parsed.departures().size());
            return new RefreshResult(sourceUrl, Status.UPDATED,
                    parsed.title() + " " + parsed.departures().size() + "건 반영");
        } catch (RuntimeException e) {
            log.error("시간표 갱신 실패(기존 시간표 유지): {}", sourceUrl, e);
            return new RefreshResult(sourceUrl, Status.FAILED, e.getMessage());
        }
    }

    private Long replace(String sourceUrl, String hash, ParsedTimetable parsed) {
        timetableRepository.findBySourceUrlAndActiveTrue(sourceUrl).forEach(ShuttleTimetable::deactivate);

        ShuttleTimetable timetable = timetableRepository.save(ShuttleTimetable.builder()
                .title(parsed.title())
                .validFrom(parsed.validFrom())
                .validTo(parsed.validTo())
                .sourceUrl(sourceUrl)
                .sourceHash(hash)
                .fetchedAt(LocalDateTime.now())
                .active(true)
                .build());

        scheduleRepository.saveAll(parsed.departures().stream()
                .map(d -> ShuttleSchedule.builder()
                        .timetable(timetable)
                        .dayType(d.dayType())
                        .direction(d.direction())
                        .departTime(d.departTime())
                        .endTime(d.endTime())
                        .note(d.note())
                        .build())
                .toList());
        return timetable.getId();
    }

    /** 파일명 "26년 셔틀버스 시간표 ..."의 연도. 없으면 올해. */
    private int yearOf(String fileName) {
        if (fileName != null) {
            Matcher m = YEAR_IN_FILE_NAME.matcher(fileName);
            if (m.find()) {
                return 2000 + Integer.parseInt(m.group(1));
            }
        }
        return Year.now().getValue();
    }

    /**
     * 원본 파일과 파서 버전을 함께 해시한다. 파서가 같은 파일을 다르게 읽게 바뀌면(분류 수정 등)
     * {@link #PARSER_VERSION}을 올려 같은 PDF도 다시 반영되게 한다.
     */
    private String sha256(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(content);
            digest.update(PARSER_VERSION.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
