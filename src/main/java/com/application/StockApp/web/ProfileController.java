package com.application.StockApp.web;

import com.application.StockApp.user.model.User;
import com.application.StockApp.user.repository.UserRepository;
import com.application.StockApp.web.dto.ProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public String profilePage(Authentication authentication, Model model) {

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        ProfileDto dto = new ProfileDto();
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());

        model.addAttribute("user", user);
        model.addAttribute("profile", dto);

        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(Authentication authentication,
                                @ModelAttribute("profile") ProfileDto form,
                                Model model) {

        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();

        user.setFullName(form.getFullName());
        user.setEmail(form.getEmail());

        userRepository.save(user);

        model.addAttribute("success", "Profile updated successfully!");
        model.addAttribute("user", user);
        model.addAttribute("profile", form);

        return "profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(Authentication authentication,
                                 @ModelAttribute("profile") ProfileDto form,
                                 Model model) {

        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();

        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("error", "New passwords do not match!");
            model.addAttribute("user", user);
            model.addAttribute("profile", form);
            return "profile";
        }

        if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPassword())) {
            model.addAttribute("error", "Current password is incorrect!");
            model.addAttribute("user", user);
            model.addAttribute("profile", form);
            return "profile";
        }

        user.setPassword(passwordEncoder.encode(form.getNewPassword()));
        userRepository.save(user);

        model.addAttribute("success", "Password changed successfully!");
        model.addAttribute("user", user);
        model.addAttribute("profile", form);

        return "profile";
    }
}
