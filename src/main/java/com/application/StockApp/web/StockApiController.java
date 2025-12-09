package com.application.StockApp.web;

import com.application.StockApp.analysis.dto.MassPoint;
import com.application.StockApp.analysis.physics.model.PeriodType;
import com.application.StockApp.analysis.physics.service.*;
import com.application.StockApp.analysis.mathematics.service.StockDeltaService;
import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.stock.repository.StockRepository;
import com.application.StockApp.records.repository.StockRecordRepository;
import com.application.StockApp.stock.service.StockService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class StockApiController {

    private static final String DEFAULT_SYMBOL = "NDAQ";

    private final StockRepository stockRepository;
    private final StockRecordRepository recordRepository;

    private final StockService stockService;
    private final StockMassService massService;
    private final StockDeltaService deltaService;
    private final StockFrequencyService frequencyService;
    private final StockKineticsService kineticsService;
    private final StockOscillationService oscillationService;
    private final StockTrajectoryService trajectoryService;

    // ================================================================
    // UTILITIES
    // ================================================================
    private Stock getStock(String code) {
        return stockRepository.findByStockCode(code)
                .orElseThrow(() -> new RuntimeException("❌ Stock not found: " + code));
    }

    private List<Map<String,Object>> filter(List<Map<String,Object>> list,
                                            LocalDate from, LocalDate to) {
        return list.stream()
                .filter(e -> {
                    LocalDate d = LocalDate.parse(e.get("date").toString());
                    return (!d.isBefore(from) && !d.isAfter(to));
                })
                .toList();
    }

    // ================================================================
    // 📌 PRICE WINDOW (using deltaService raw price series)
    // ================================================================
    @GetMapping("/stocks/{code}/records-window")
    public List<Map<String,Object>> priceWindow(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<Map<String,Object>> raw = deltaService.getRawPriceSeries(code);
        return filter(raw, from, to);
    }

    // ================================================================
    // ⚖ MASS WINDOW
    // ================================================================
    @GetMapping("/stocks/{code}/mass-window")
    public List<Map<String,Object>> massWindow(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        // Ползва реалния massService.getMassPoints()
        List<MassPoint> points = massService.getMassPoints(getStock(code));

        return points.stream()
                .filter(p -> !p.date().isBefore(from) && !p.date().isAfter(to))
                .map(p ->
                        Map.<String,Object>of(
                                "date", p.date().toString(),
                                "mass", p.mass()
                        )
                )
                .toList();
    }

    // ================================================================
    // 🔁 FREQUENCY WINDOW (daily / weekly / monthly / yearly)
    // ================================================================
    @GetMapping("/stocks/{code}/frequency/{period}")
    public List<Map<String,Object>> frequencyWindow(
            @PathVariable String code,
            @PathVariable String period,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        PeriodType type = PeriodType.valueOf(period.toUpperCase());

        return frequencyService.getFrequencyWindow(
                getStock(code),
                type,
                from,
                to
        );
    }

    // ================================================================
    // Δ DELTA WINDOW
    // ================================================================
    @GetMapping("/stocks/{code}/delta-window")
    public List<Map<String,Object>> deltaWindow(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<Map<String,Object>> raw = deltaService.getRawPriceSeries(code);
        return filter(raw, from, to);
    }

    // ================================================================
    // ⚡ KINETICS WINDOW
    // ================================================================
    @GetMapping("/stocks/{code}/kinetics-window")
    public List<Map<String,Object>> kineticsWindow(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<Map<String,Object>> raw = kineticsService.getKineticsSeries(getStock(code));
        return filter(raw, from, to);
    }

    // ================================================================
    // 🌀 OSCILLATION WINDOW
    // ================================================================
    @GetMapping("/stocks/{code}/oscillation-window")
    public List<Map<String,Object>> oscillationWindow(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<Map<String,Object>> raw = oscillationService.getOscillationData(getStock(code));
        return filter(raw, from, to);
    }

    // ================================================================
    // ✈ TRAJECTORY WINDOW
    // ================================================================
    @GetMapping("/stocks/{code}/trajectory-window")
    public List<Map<String,Object>> trajectoryWindow(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        // Ще приемем, че имаш метод getTrajectoryData(stock)
        List<Map<String,Object>> raw = trajectoryService.getTrajectoryData(getStock(code));
        return filter(raw, from, to);
    }

    // ================================================================
    // 📈 INDEX PAGE CHART → /api/chart?range=X
    // ================================================================
    @GetMapping("/chart")
    public List<Map<String,Object>> indexChart(
            @RequestParam(defaultValue = "monthly") String range
    ) {
        // 1) Вземаме всички дати, за да изберем последната
        List<Object[]> all = recordRepository.aggregateAllNative(
                DEFAULT_SYMBOL,
                LocalDate.of(1900,1,1),
                LocalDate.of(2100,1,1)
        );

        if (all.isEmpty()) {
            return List.of();
        }

        LocalDate lastDate = ((java.sql.Date) all.get(all.size()-1)[0]).toLocalDate();
        LocalDate from;

        switch (range.toLowerCase()) {
            case "weekly"  -> from = lastDate.minusWeeks(1);
            case "monthly" -> from = lastDate.minusMonths(1);
            case "yearly"  -> from = lastDate.minusYears(1);
            case "all"     -> from = LocalDate.of(1900,1,1);
            default        -> from = lastDate.minusMonths(1);
        }

        // 2) Връщаме агрегирани точки, както очаква front-end
        return stockService.getAggregatedStockData(DEFAULT_SYMBOL, from, lastDate, range)
                .stream()
                .map(dto -> Map.<String,Object>of(
                        "date", dto.date().toString(),
                        "close", dto.close()
                ))
                .collect(Collectors.toList());
    }
}
