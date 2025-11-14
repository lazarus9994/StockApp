package com.application.StockApp.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRegisterDto(

        @NotBlank
        String username,

        @NotBlank
        String fullName,

        @Email
        String email,

        @NotBlank
        String password
) {}
