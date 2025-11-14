package com.application.StockApp.web;

import com.application.StockApp.analysis.service.StockFrequencyService;
import com.application.StockApp.analysis.service.StockMassService;
import com.application.StockApp.records.model.StockRecord;
import com.application.StockApp.records.repository.StockRecordRepository;
import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.stock.repository.StockRepository;
import com.application.StockApp.analysis.model.StockFrequency.PeriodType;

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
    private final StockMassService stockMassService;
    private final StockFrequencyService  stockFrequencyService;



    @GetMapping("/analytics")
    public String analytics(Model model) {
        model.addAttribute("stocks", stockRepository.findAll());
        return "analytics";
    }

    @GetMapping("/analytics/{code}")
    public String analyticsForStock(@PathVariable String code, Model model) {

        Stock stock = stockRepository.findByStockCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException("Stock not found: " + code));

        List<StockRecord> records = recordRepository.findAllByStock(stock)
                .stream()
                .sorted(Comparator.comparing(StockRecord::getDate))
                .toList();

        // ===============================
        // PRICE SERIES
        // ===============================
        List<String> dates = records.stream()
                .map(r -> r.getDate().toString())
                .toList();

        List<Double> closes = records.stream()
                .map(r -> r.getClose().doubleValue())
                .toList();

        // ===============================
        // MASS SERIES
        // ===============================
        var masses = stockMassService.getMassPoints(stock); // -> List<MassPoint>

        List<String> massDates = masses.stream().map(m -> m.date().toString()).toList();
        List<Double> massValues = masses.stream().map(m -> m.mass().doubleValue()).toList();

        // ===============================
        // FREQUENCY SERIES
        // ===============================
        var daily = stockFrequencyService.getFrequencyPoints(stock, PeriodType.DAILY);
        var weekly = stockFrequencyService.getFrequencyPoints(stock, PeriodType.WEEKLY);
        var monthly = stockFrequencyService.getFrequencyPoints(stock, PeriodType.MONTHLY);
        var yearly = stockFrequencyService.getFrequencyPoints(stock, PeriodType.YEARLY);

        model.addAttribute("stock", stock);

        model.addAttribute("dates", dates);
        model.addAttribute("closes", closes);

        model.addAttribute("massDates", massDates);
        model.addAttribute("massValues", massValues);

        model.addAttribute("daily", daily);
        model.addAttribute("weekly", weekly);
        model.addAttribute("monthly", monthly);
        model.addAttribute("yearly", yearly);

        return "analytics";
    }


}

