package com.tukorea.bus.temp.repository;

import com.tukorea.bus.temp.entity.Temp;
import com.tukorea.bus.temp.repository.custom.TempRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public interface TempRepository extends JpaRepository<Temp, String>, TempRepositoryCustom {

    
}
