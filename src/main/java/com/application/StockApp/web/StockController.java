package com.application.StockApp.web;

import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/stocks")
public class StockController {

    private final StockRepository stockRepository;

    @GetMapping
    public String listStocks(Model model) {
        model.addAttribute("stocks", stockRepository.findAll());
        return "stocks";  // loads stocks.html
    }

    @GetMapping("/{code}")
    public String stockDetails(@PathVariable String code, Model model) {

        Stock stock = stockRepository.findByStockCode(code)
                .orElseThrow(() -> new RuntimeException("Stock not found: " + code));

        model.addAttribute("stock", stock);
        model.addAttribute("code", code);

        return "stock-details";
    }
}
