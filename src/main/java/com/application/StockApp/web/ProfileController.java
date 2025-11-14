package com.application.StockApp.web;

import com.application.StockApp.stock.service.StockService;
import com.application.StockApp.user.model.User;
import com.application.StockApp.user.repository.UserRepository;
import com.application.StockApp.watchlist.service.WatchlistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.application.StockApp.web.dto.StockRecordDto;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WatchlistService watchlistService;
    @GetMapping("/profile")
    public String profilePage(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(Authentication authentication,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Model model) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "New passwords do not match!");
            model.addAttribute("user", user);
            return "profile";
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            model.addAttribute("error", "Current password is incorrect!");
            model.addAttribute("user", user);
            return "profile";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        model.addAttribute("success", "Password changed successfully!");
        model.addAttribute("user", user);
        return "profile";
    }



    @GetMapping("/home")
    public String userHome(Model model, Principal principal) {
        model.addAttribute("userStocks", watchlistService.getUserWatchlist(principal));
        return "home";
    }


}
