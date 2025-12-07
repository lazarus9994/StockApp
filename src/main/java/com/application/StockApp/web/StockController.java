package com.application.StockApp.web;

import com.application.StockApp.analysis.mathematics.service.StockDeltaService;
import com.application.StockApp.analysis.physics.model.PeriodType;
import com.application.StockApp.analysis.physics.service.*;
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
@RequiredArgsConstructor
@RequestMapping("/stocks")
public class StockController {

    private final StockRepository stockRepo;
    private final StockMassService massService;
    private final StockFrequencyService frequencyService;
    private final StockDeltaService deltaService;
    private final StockKineticsService kineticsService;
    private final StockOscillationService oscillationService;

    @GetMapping
    public String stocksPage(Model model) {
        model.addAttribute("stocks", stockRepo.findAll());
        return "stocks";
    }

    @GetMapping("/{code}")
    public String stockDetails(@PathVariable String code, Model model) {
        Stock stock = stockRepo.findByStockCodeIgnoreCase(code)
                .orElseThrow(() -> new IllegalArgumentException("Unknown stock: " + code));
        model.addAttribute("stock", stock);
        return "stocks";
    }

    private Stock getStock(String code) {
        return stockRepo.findByStockCodeIgnoreCase(code)
                .orElseThrow(() -> new IllegalArgumentException("Unknown stock: " + code));
    }

    // -----------------------------
    // MASS WINDOW
    // -----------------------------
    @GetMapping("/api/{code}/mass-window")
    @ResponseBody
    public List<Map<String,Object>> massWindow(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return massService.getMassWindow(getStock(code), from, to);
    }

    // -----------------------------
    // FREQUENCY WINDOW
    // -----------------------------
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

    // -----------------------------
    // DELTA WINDOW
    // -----------------------------
    @GetMapping("/api/{code}/delta-window")
    @ResponseBody
    public List<Map<String,Object>> deltaWindow(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return deltaService.getDeltaWindow(getStock(code), from, to);
    }

    // -----------------------------
    // KINETICS WINDOW
    // -----------------------------
    @GetMapping("/api/{code}/kinetics-window")
    @ResponseBody
    public List<Map<String,Object>> kineticsWindow(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return kineticsService.getKineticsWindow(getStock(code), from, to);
    }

    // -----------------------------
    // OSCILLATION WINDOW
    // -----------------------------
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
