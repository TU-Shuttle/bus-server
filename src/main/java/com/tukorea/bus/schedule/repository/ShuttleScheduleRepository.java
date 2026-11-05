package com.tukorea.bus.schedule.repository;

import com.tukorea.bus.schedule.entity.ShuttleDayType;
import com.tukorea.bus.schedule.entity.ShuttleDirection;
import com.tukorea.bus.schedule.entity.ShuttleSchedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShuttleScheduleRepository extends JpaRepository<ShuttleSchedule, Long> {

    /** 활성 시간표의 시간표 줄만 조회한다. dayType/direction이 null이면 해당 조건은 무시한다. */
    @Query("""
            select s from ShuttleSchedule s
            where s.timetable.active = true
              and (:dayType is null or s.dayType = :dayType)
              and (:direction is null or s.direction = :direction)
            order by s.dayType, s.direction, s.departTime
            """)
    List<ShuttleSchedule> findActive(@Param("dayType") ShuttleDayType dayType,
                                     @Param("direction") ShuttleDirection direction);
}
