package com.tukorea.bus.common.converter;

public interface Converter<S, T> {

    T convert(S source);
}
