package com.application.StockApp.web;

import com.application.StockApp.analysis.mathematics.service.StockDeltaService;
import com.application.StockApp.analysis.physics.model.PeriodType;
import com.application.StockApp.analysis.physics.service.*;
import com.application.StockApp.records.repository.StockRecordRepository;
import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final StockRepository stockRepo;
    private final StockRecordRepository recordRepo;

    private final StockMassService massService;
    private final StockFrequencyService frequencyService;
    private final StockDeltaService deltaService;
    private final StockKineticsService kineticsService;
    private final StockOscillationService oscillationService;

    @GetMapping
    public String analyticsPage(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model
    ) {
        List<Stock> stocks = stockRepo.findAll();
        model.addAttribute("stocks", stocks);

        if (code == null && !stocks.isEmpty()) {
            code = stocks.get(0).getStockCode();
        }

        Stock stock = stockRepo.findByStockCode(code).orElse(null);
        model.addAttribute("selectedCode", code);

        if (from == null) from = LocalDate.now().minusYears(1);
        if (to == null) to = LocalDate.now();

        model.addAttribute("fromDate", from);
        model.addAttribute("toDate", to);

        return "analytics";
    }

    private Stock getStock(String code) {
        return stockRepo.findByStockCodeIgnoreCase(code)
                .orElseThrow(() -> new IllegalArgumentException("Unknown stock: " + code));
    }

    // -------------------------------
    // PRICE WINDOW (close prices)
    // -------------------------------
    @GetMapping("/api/{code}/price-window")
    @ResponseBody
    public ResponseEntity<?> priceWindow(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        Stock stock = getStock(code);

        var list = recordRepo.findAllByStockAndDateBetweenOrderByDateAsc(stock, from, to)
                .stream()
                .map(r -> Map.of(
                        "date", r.getDate().toString(),
                        "close", r.getClose()
                ))
                .toList();

        return ResponseEntity.ok(list);
    }

    // -------------------------------
    // MASS WINDOW
    // -------------------------------
    @GetMapping("/api/{code}/mass-window")
    @ResponseBody
    public List<Map<String,Object>> massWindow(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return massService.getMassWindow(getStock(code), from, to);
    }

    // -------------------------------
    // FREQUENCY WINDOWS
    // -------------------------------
    @GetMapping("/api/{code}/frequency-window")
    @ResponseBody
    public List<Map<String,Object>> frequencyWindow(
            @PathVariable String code,
            @RequestParam PeriodType period,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return frequencyService.getFrequencyWindow(getStock(code), period, from, to);
    }

    // -------------------------------
    // DELTAS
    // -------------------------------
    @GetMapping("/api/{code}/delta-window")
    @ResponseBody
    public List<Map<String,Object>> deltaWindow(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return deltaService.getDeltaWindow(getStock(code), from, to);
    }

    // -------------------------------
    // KINETICS
    // -------------------------------
    @GetMapping("/api/{code}/kinetics-window")
    @ResponseBody
    public List<Map<String,Object>> kineticsWindow(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return kineticsService.getKineticsWindow(getStock(code), from, to);
    }

    // -------------------------------
    // OSCILLATION
    // -------------------------------
    @GetMapping("/api/{code}/oscillation-window")
    @ResponseBody
    public List<Map<String,Object>> oscillationWindow(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return oscillationService.getOscillationWindow(getStock(code), from, to);
    }
}
