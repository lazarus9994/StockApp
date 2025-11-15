package com.application.StockApp.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ReportsController {

    @GetMapping("/reports")
    public String reportsPage(Model model) {

        // TODO: Add dynamic reports later
        model.addAttribute("status", "Reports module is working!");

        return "reports";
    }
}
