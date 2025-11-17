package com.application.StockApp.web;

import com.application.StockApp.news.service.NewsService;
import com.application.StockApp.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ReportsController {

    private final StockRepository stockRepository;
    private final NewsService newsService;

    @GetMapping("/reports")
    public String reports(@RequestParam(required = false) String stock,
                          Model model) {

        model.addAttribute("stocks", stockRepository.findAll());

        if (stock != null && !stock.isBlank()) {
            model.addAttribute("selectedStock", stock);
            model.addAttribute("news", newsService.getByStock(stock));
        }

        return "reports";
    }
}
