package com.application.StockApp.configuration;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class GlobalModelAttributes {

    @ModelAttribute
    public void addCurrentPath(Model model, HttpServletRequest request) {
        model.addAttribute("currentPath", request.getRequestURI());
    }

}
