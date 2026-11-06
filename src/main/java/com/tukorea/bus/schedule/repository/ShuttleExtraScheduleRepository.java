package com.tukorea.bus.schedule.repository;

import com.tukorea.bus.schedule.entity.ShuttleExtraSchedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShuttleExtraScheduleRepository extends JpaRepository<ShuttleExtraSchedule, Long> {

    List<ShuttleExtraSchedule> findAllByOrderByDirectionAscDepartTimeAsc();
}
