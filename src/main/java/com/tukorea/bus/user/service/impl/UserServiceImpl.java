package com.tukorea.bus.user.service.impl;

import com.tukorea.bus.common.response.code.BusinessErrorCode;
import com.tukorea.bus.common.response.exception.BusinessException;
import com.tukorea.bus.user.converter.UserToDtoConverter;
import com.tukorea.bus.user.dto.SignInUserRequest;
import com.tukorea.bus.user.dto.SignInUserResponse;
import com.tukorea.bus.user.dto.SignUpUserRequest;
import com.tukorea.bus.user.entity.User;
import com.tukorea.bus.user.repository.UserRepository;
import com.tukorea.bus.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserToDtoConverter userToDtoConverter;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void signUpUser(SignUpUserRequest signUpUserRequest) {
        if (userRepository.existsUserById(signUpUserRequest.id())) {
            throw new BusinessException(BusinessErrorCode.SAVE_FAILED);
        }
        User user = User.builder()
                .id(signUpUserRequest.id())
                .password(passwordEncoder.encode(signUpUserRequest.password()))
                .build();
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public SignInUserResponse signInUser(SignInUserRequest signInUserRequest) {
        User user = userRepository.findUserById(signInUserRequest.id())
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.RESOURCE_NOT_FOUND));

        if (!passwordEncoder.matches(signInUserRequest.password(), user.getPassword())) {
            throw new BusinessException(BusinessErrorCode.RESOURCE_NOT_FOUND);
        }

        return userToDtoConverter.convert(user);
    }
}
