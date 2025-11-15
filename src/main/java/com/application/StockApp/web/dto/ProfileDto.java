package com.application.StockApp.web.dto;

import lombok.Data;

@Data
public class ProfileDto {

    private String fullName;
    private String email;

    // Тези три се подават *само* при смяна на парола
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}
