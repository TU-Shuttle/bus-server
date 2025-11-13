package com.tukorea.bus.temp.repository.custom.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tukorea.bus.temp.repository.custom.TempRepositoryCustom;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TempRepositoryCustomImpl implements TempRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    
}
