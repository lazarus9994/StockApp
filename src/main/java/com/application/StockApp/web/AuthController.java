package com.application.StockApp.web;

import com.application.StockApp.user.service.UserService;
import com.application.StockApp.web.dto.UserRegisterDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(HttpServletRequest request, Model model) {
        model.addAttribute("currentPath", request.getRequestURI());
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(HttpServletRequest request, Model model) {
        model.addAttribute("currentPath", request.getRequestURI());
        model.addAttribute("userRegisterDto", new UserRegisterDto(null, null, null, null));
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("userRegisterDto") UserRegisterDto userRegisterDto) {
        userService.register(userRegisterDto);
        return "redirect:/login?registered";
    }
}
