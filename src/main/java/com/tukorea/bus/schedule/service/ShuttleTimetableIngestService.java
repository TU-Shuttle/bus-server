package com.tukorea.bus.schedule.service;

import java.util.List;

public interface ShuttleTimetableIngestService {

    /** 설정된 모든 원본에서 시간표를 수집해 변경된 것만 반영한다. 원본별 결과를 돌려준다. */
    List<RefreshResult> refreshAll();

    enum Status {
        /** 새 시간표를 반영했다 */
        UPDATED,
        /** 원본이 바뀌지 않아 그대로 뒀다 */
        UNCHANGED,
        /** 실패했다. 기존 시간표는 유지된다 */
        FAILED
    }

    record RefreshResult(String sourceUrl, Status status, String message) {
    }
}
