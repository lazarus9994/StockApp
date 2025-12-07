package com.application.StockApp.web;

import com.application.StockApp.web.dto.UserRegisterDto;
import com.application.StockApp.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;

    /** Добавя текущия path към модела за layout.html */
    private void addCurrentPath(Model model, HttpServletRequest request) {
        model.addAttribute("currentPath", request.getRequestURI());
    }

    // -----------------------
    // GET /login
    // -----------------------
    @GetMapping("/login")
    public String loginPage(Model model, HttpServletRequest request) {
        addCurrentPath(model, request);
        return "login";
    }

    // -----------------------
    // GET /register
    // -----------------------
    @GetMapping("/register")
    public String registerPage(Model model, HttpServletRequest request) {
        addCurrentPath(model, request);
        model.addAttribute("userRegisterDto", new UserRegisterDto(null, null, null, null));
        return "register";
    }

    // -----------------------
    // POST /register
    // -----------------------
    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("userRegisterDto") UserRegisterDto form,
            BindingResult result,
            Model model,
            HttpServletRequest request) {

        addCurrentPath(model, request);

        if (result.hasErrors()) {
            log.warn("Registration failed: invalid input");
            return "register";
        }

        try {
            userService.register(form);
        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }

        return "redirect:/login?registered";
    }

    // -----------------------
    // GET /logout
    // (Spring Security обработва logout, но ние правим redirect)
    // -----------------------
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, Model model) {
        addCurrentPath(model, request);
        return "redirect:/login?logout";
    }
}
