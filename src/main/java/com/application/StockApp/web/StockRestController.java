package com.application.StockApp.web;

import com.application.StockApp.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockRestController {

    private final StockService stockService;

    @GetMapping("/codes")
    public List<String> getAllStockCodes() {
        return stockService.getAllStockCodes();
    }
}
