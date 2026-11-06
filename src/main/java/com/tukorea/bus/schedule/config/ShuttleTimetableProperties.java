package com.tukorea.bus.schedule.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @param enabled     false면 주기 수집/시작 시 수집을 하지 않는다(수동 트리거는 가능)
 * @param sourceUrls  시간표 첨부파일 주소들. 내용(해시)이 같으면 한 번만 반영한다
 * @param refreshCron 주기 수집 cron(Asia/Seoul)
 */
@ConfigurationProperties(prefix = "shuttle.timetable")
public record ShuttleTimetableProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue({
                "https://tukorea.ac.kr/viewer/tukorea/15/fileDown1/fileDownload.do",
                "https://tukorea.ac.kr/viewer/tukorea/15/fileDown2/fileDownload.do"
        }) List<String> sourceUrls,
        @DefaultValue("0 0 4 * * *") String refreshCron
) {
}
