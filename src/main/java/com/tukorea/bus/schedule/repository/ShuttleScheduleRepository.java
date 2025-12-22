package com.tukorea.bus.schedule.repository;

import com.tukorea.bus.schedule.entity.ShuttleSchedule;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShuttleScheduleRepository extends JpaRepository<ShuttleSchedule, Long> {

    List<ShuttleSchedule> findByDayTypeAndDirectionOrderByDepartTimeAsc(String dayType, String direction);

    List<ShuttleSchedule> findAllByOrderByDayTypeAscDirectionAscDepartTimeAsc();
}

