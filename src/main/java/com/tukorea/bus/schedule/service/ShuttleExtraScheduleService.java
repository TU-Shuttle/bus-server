package com.tukorea.bus.schedule.service;

import com.tukorea.bus.schedule.dto.ReplaceExtraTimetableRequest;
import com.tukorea.bus.schedule.entity.ShuttleExtraSchedule;
import java.util.List;

public interface ShuttleExtraScheduleService {

    /** 기존 추가 운행을 모두 지우고 요청 내용으로 바꾼다. 저장한 줄 수를 돌려준다. */
    int replace(ReplaceExtraTimetableRequest request);

    /** 오늘 기준 적용 기간이 끝나지 않은 추가 운행. */
    List<ShuttleExtraSchedule> findActive();
}
