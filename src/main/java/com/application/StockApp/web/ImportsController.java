package com.application.StockApp.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ImportsController {

    @GetMapping("/imports")
    public String importsPage(Model model) {

        // TODO: Load real import logs
        model.addAttribute("imports", java.util.List.of(
                "Sample import 1",
                "Sample import 2"
        ));

        return "imports";
    }
}
