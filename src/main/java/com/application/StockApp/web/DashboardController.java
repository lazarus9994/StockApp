package com.application.StockApp.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.application.StockApp.dashboard.service.DashboardData;
import com.application.StockApp.dashboard.service.DashboardService;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final com.application.StockApp.dashboard.service.DashboardService dashboardService;

    @GetMapping("/")
    public String dashboard(@RequestParam(defaultValue = "monthly") String range,
                            Model model,
                            Principal principal) throws JsonProcessingException {

        System.out.println("🟢 DashboardController reached");

        DashboardData data = dashboardService.loadDashboardData(principal);

        model.addAttribute("symbol", data.firstSymbolOrDefault());
        model.addAttribute("range", range);
        model.addAttribute("inputCode", "");
        model.addAttribute("watchlist", data.watchlist());
        model.addAttribute("changes", data.changes());

        return "index";
    }
}
