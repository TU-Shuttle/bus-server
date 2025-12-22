package com.tukorea.bus.user.repository.custom.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tukorea.bus.user.repository.custom.UserRepositoryCustom;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    
}
