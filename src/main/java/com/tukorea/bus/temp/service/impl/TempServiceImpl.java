package com.tukorea.bus.temp.service.impl;

import com.tukorea.bus.common.converter.ConverterUtil;
import com.tukorea.bus.temp.converter.TempToDtoConverter;
import com.tukorea.bus.temp.dto.FindTempResponse;
import com.tukorea.bus.temp.entity.Temp;
import com.tukorea.bus.temp.repository.TempRepository;
import com.tukorea.bus.temp.service.TempService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TempServiceImpl implements TempService {

    private final TempRepository tempRepository;
    private final TempToDtoConverter tempToDtoConverter;


    @Override
    @Transactional(readOnly = true)
    public List<FindTempResponse> findTemps() {
        List<Temp> temps = tempRepository.findAll();
        return ConverterUtil.convertList(temps, tempToDtoConverter);
    }
}
