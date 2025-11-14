package com.application.StockApp.web;

import com.application.StockApp.records.model.StockRecord;
import com.application.StockApp.records.repository.StockRecordRepository;
import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.stock.repository.StockRepository;
import com.application.StockApp.analysis.service.StockAnalysisService;
import com.application.StockApp.analysis.service.StockAnalysisBatchService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AnalyticsController {

    private final StockRepository stockRepository;
    private final StockRecordRepository recordRepository;

    private final StockAnalysisService stockAnalysisService;     // ✅ ДОБАВЕНО
    private final StockAnalysisBatchService batchService;        // ✅ ДОБАВЕНО

    @GetMapping("/analytics")
    public String analytics(Model model) {
        List<Stock> allStocks = stockRepository.findAll();

        if (!allStocks.isEmpty()) {
            Stock first = allStocks.get(0);
            List<StockRecord> records = recordRepository.findAll().stream()
                    .filter(r -> r.getStock().getId().equals(first.getId()))
                    .sorted(Comparator.comparing(StockRecord::getDate))
                    .limit(10)
                    .collect(Collectors.toList());

            List<String> dates = records.stream()
                    .map(r -> r.getDate().toString())
                    .toList();

            List<Double> closes = records.stream()
                    .map(r -> r.getClose().doubleValue())
                    .toList();

            model.addAttribute("stock", first);
            model.addAttribute("dates", dates);
            model.addAttribute("closes", closes);
        }

        return "analytics";
    }

    @GetMapping("/analyze/{code}")
    @ResponseBody
    public String analyzeStock(@PathVariable String code) {
        Stock stock = stockRepository.findByStockCode(code)
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        stockAnalysisService.buildSummary(stock);    // или analyzeStock(), ако ползваш стария метод

        return "✅ Done analyzing " + code;
    }

    @GetMapping("/analyze/history/all")
    @ResponseBody
    public String analyzeHistoryAll() {
        batchService.analyzeAllStocksHistory();
        return "OK";
    }
}
