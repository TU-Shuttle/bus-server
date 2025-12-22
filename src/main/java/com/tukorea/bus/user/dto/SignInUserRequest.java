package com.tukorea.bus.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignInUserRequest(

        @NotBlank
        @Size(min = 1, max = 250)
        String id,

        @NotBlank
        @Size(min = 1, max = 500)
        String password
) {
}