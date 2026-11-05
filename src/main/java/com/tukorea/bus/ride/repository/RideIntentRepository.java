package com.tukorea.bus.ride.repository;

import com.tukorea.bus.ride.entity.RideIntent;
import com.tukorea.bus.ride.entity.ShuttleStop;
import com.tukorea.bus.schedule.entity.ShuttleDirection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RideIntentRepository extends JpaRepository<RideIntent, Long> {

    Optional<RideIntent> findByDeviceIdAndServiceDateAndDirectionAndDepartTime(
            String deviceId, LocalDate serviceDate, ShuttleDirection direction, LocalTime departTime);

    List<RideIntent> findByDeviceIdAndServiceDateGreaterThanEqualOrderByServiceDateAscDepartTimeAsc(
            String deviceId, LocalDate from);

    long countByDeviceIdAndServiceDateGreaterThanEqual(String deviceId, LocalDate from);

    @Query("""
            select r.direction as direction, r.departTime as departTime, r.dropOff as dropOff, count(r) as riders
            from RideIntent r
            where r.serviceDate = :date
            group by r.direction, r.departTime, r.dropOff
            order by r.departTime
            """)
    List<RideCount> countByDate(@Param("date") LocalDate date);

    interface RideCount {
        ShuttleDirection getDirection();

        LocalTime getDepartTime();

        ShuttleStop getDropOff();

        long getRiders();
    }
}
