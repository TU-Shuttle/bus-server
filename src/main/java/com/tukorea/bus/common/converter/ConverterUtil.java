package com.tukorea.bus.common.converter;

import java.util.List;
import java.util.stream.Collectors;

public class ConverterUtil {

    public static <S, T> List<T> convertList(List<S> sourceList, Converter<S, T> converter) {
        return sourceList.stream()
                .map(converter::convert)
                .collect(Collectors.toList());
    }
}