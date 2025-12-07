package com.application.StockApp.web;

import com.application.StockApp.web.dto.ProfileDto;
import com.application.StockApp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    // -------------------------
    // VIEW: /user/profile
    // -------------------------
    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal UserDetails principal, Model model) {
        String username = principal.getUsername();
        ProfileDto dto = userService.getProfile(username);

        model.addAttribute("profileDto", dto);
        return "profile"; // profile.html
    }

    // -------------------------
    // POST: update profile data
    // -------------------------
    @PostMapping("/profile")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails principal,
            @ModelAttribute("profileDto") ProfileDto dto
    ) {
        String username = principal.getUsername();
        userService.updateProfile(username, dto);

        return "redirect:/user/profile?updated";
    }


    // -------------------------
    // VIEW: /user/settings
    // -------------------------
    @GetMapping("/settings")
    public String settingsPage(@AuthenticationPrincipal UserDetails principal, Model model) {
        String username = principal.getUsername();
        ProfileDto dto = userService.getProfile(username);

        model.addAttribute("profileDto", dto);
        return "settings"; // settings.html
    }

    // -------------------------
    // POST: update settings
    // -------------------------
    @PostMapping("/settings")
    public String updateSettings(
            @AuthenticationPrincipal UserDetails principal,
            @ModelAttribute("profileDto") ProfileDto dto
    ) {
        String username = principal.getUsername();
        userService.updateProfile(username, dto);

        return "redirect:/user/settings?saved";
    }
}
