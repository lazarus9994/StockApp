package com.application.StockApp.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {

    @NotBlank
    private String fullName;

    @Email
    @NotBlank
    private String email;

    // тези три полета се ползват само при смяна на парола
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}
