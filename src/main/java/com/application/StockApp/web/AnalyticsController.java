package com.application.StockApp.web;

import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/analytics")
public class AnalyticsController {

    private final StockRepository stockRepository;

    @GetMapping
    public String analyticsPage(
            @RequestParam(required = false, defaultValue = "NDAQ") String code,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model
    ) {

        // Логика за fallback диапазон
        if (from == null) {
            from = LocalDate.now().minusMonths(1);
        }
        if (to == null) {
            to = LocalDate.now();
        }

        // Зареждаме всички акции за dropdown менюто
        List<Stock> stocks = stockRepository.findAll();

        model.addAttribute("stocks", stocks);
        model.addAttribute("selectedCode", code);
        model.addAttribute("fromDate", from);
        model.addAttribute("toDate", to);

        // Това рендва templates/analytics.html
        return "analytics";
    }
}
