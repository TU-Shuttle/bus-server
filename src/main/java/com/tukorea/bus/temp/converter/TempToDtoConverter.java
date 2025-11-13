package com.tukorea.bus.temp.converter;

import com.tukorea.bus.common.converter.Converter;
import com.tukorea.bus.temp.dto.FindTempResponse;
import com.tukorea.bus.temp.entity.Temp;
import org.springframework.stereotype.Component;

@Component
public class TempToDtoConverter implements Converter<Temp, FindTempResponse> {

    @Override
    public FindTempResponse convert(Temp source) {
        return new FindTempResponse(source.getTemp());
    }
}
