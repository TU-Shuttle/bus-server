package com.tukorea.bus.temp.service;

import com.tukorea.bus.temp.dto.FindTempResponse;
import java.util.List;

public interface TempService {

    List<FindTempResponse> findTemps();
}
