package com.tukorea.bus.user.service;

import com.tukorea.bus.user.dto.SignInUserRequest;
import com.tukorea.bus.user.dto.SignInUserResponse;
import com.tukorea.bus.user.dto.SignUpUserRequest;

public interface UserService {

    /**
     * 회원 가입 Service
     *
     * @param signUpUserRequest
     * @return void
     */
    void signUpUser(SignUpUserRequest signUpUserRequest);

    /**
     * 회원 접속 Service
     *
     * @param signInUserRequest
     * @return SignInUserResponse
     */
    SignInUserResponse signInUser(SignInUserRequest signInUserRequest);
}
