package com.tukorea.bus.user.controller;

import com.tukorea.bus.common.response.code.SuccessCode;
import com.tukorea.bus.common.response.dto.SuccessResponse;
import com.tukorea.bus.common.response.handler.ResponseHandler;
import com.tukorea.bus.user.dto.SignInUserRequest;
import com.tukorea.bus.user.dto.SignInUserResponse;
import com.tukorea.bus.user.dto.SignUpUserRequest;
import com.tukorea.bus.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/user/signup")
    @Operation(summary = "회원 가입", description = "신규 회원이 가입할 수 있도록 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", ref = "Ok"),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "500", ref = "InternalServerError")
    })
    public ResponseEntity<SuccessResponse> signUpUser(
            @RequestBody @Valid SignUpUserRequest signUpUserRequest
    ) {
        userService.signUpUser(signUpUserRequest);
        return ResponseHandler.success(SuccessCode.REGISTER_SUCCESS);
    }

    @PostMapping("/user/signin")
    @Operation(summary = "회원 접속", description = "회원이 접속할 수 있도록 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", ref = "Ok"),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "500", ref = "InternalServerError")
    })
    public ResponseEntity<SuccessResponse> signInUser(
            @RequestBody @Valid SignInUserRequest signInUserRequest,
            HttpSession session
    ) {
        SignInUserResponse signInUserResponse = userService.signInUser(signInUserRequest);
        
        session.setAttribute("id", signInUserResponse.id());

        return ResponseHandler.success(SuccessCode.RESOURCE_FOUND);
    }
}
