package com.tukorea.bus.schedule.repository;

import com.tukorea.bus.schedule.entity.ShuttleTimetable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShuttleTimetableRepository extends JpaRepository<ShuttleTimetable, Long> {

    List<ShuttleTimetable> findBySourceUrlAndActiveTrue(String sourceUrl);

    Optional<ShuttleTimetable> findFirstBySourceHashAndActiveTrue(String sourceHash);

    boolean existsByActiveTrue();

    Optional<ShuttleTimetable> findFirstByActiveTrueOrderByFetchedAtDesc();
}
