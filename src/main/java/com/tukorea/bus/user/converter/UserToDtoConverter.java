package com.tukorea.bus.user.converter;

import com.tukorea.bus.common.converter.Converter;
import com.tukorea.bus.user.dto.SignInUserResponse;
import com.tukorea.bus.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserToDtoConverter implements Converter<User, SignInUserResponse> {

    @Override
    public SignInUserResponse convert(User source) {
        return new SignInUserResponse(source.getId());
    }
}
