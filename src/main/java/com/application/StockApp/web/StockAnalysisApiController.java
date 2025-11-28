package com.application.StockApp.web;

import com.application.StockApp.analysis.dto.FrequencyPoint;
import com.application.StockApp.analysis.dto.MassPoint;
import com.application.StockApp.analysis.dto.StockAnalysisResponse;
import com.application.StockApp.analysis.physics.model.PeriodType;
import com.application.StockApp.analysis.physics.repository.StockFrequencyRepository;
import com.application.StockApp.analysis.physics.repository.StockMassRepository;
import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analysis")
public class StockAnalysisApiController {

    private final StockRepository stockRepository;
    private final StockMassRepository massRepo;
    private final StockFrequencyRepository freqRepo;

    @GetMapping("/{code}")
    public StockAnalysisResponse getAnalysis(@PathVariable String code) {

        Stock stock = stockRepository.findByStockCodeIgnoreCase(code)
                .orElseThrow(() -> new IllegalArgumentException("Unknown stock: " + code));

        // --- Масите: взимаме само НЕагрегирани маси (дневни) ---
        var masses = massRepo.findAllByStockOrderByDateAsc(stock).stream()
                .filter(m -> !Boolean.TRUE.equals(m.getAggregated())) // само дневни маси
                .map(m -> new MassPoint(m.getDate(), m.getMass()))
                .toList();

        // --- Седмични честоти ---
        var weekly = freqRepo.findAllByStockAndPeriodType(stock, PeriodType.WEEKLY)
                .stream()
                .sorted((a, b) -> a.getPeriodStart().compareTo(b.getPeriodStart()))
                .map(f -> new FrequencyPoint(
                        f.getPeriodStart(),
                        f.getFrequencyValue()
                ))
                .toList();

        // --- Месечни честоти ---
        var monthly = freqRepo.findAllByStockAndPeriodType(stock, PeriodType.MONTHLY)
                .stream()
                .sorted((a, b) -> a.getPeriodStart().compareTo(b.getPeriodStart()))
                .map(f -> new FrequencyPoint(
                        f.getPeriodStart(),
                        f.getFrequencyValue()
                ))
                .toList();

        // --- Годишни честоти ---
        var yearly = freqRepo.findAllByStockAndPeriodType(stock, PeriodType.YEARLY)
                .stream()
                .sorted((a, b) -> a.getPeriodStart().compareTo(b.getPeriodStart()))
                .map(f -> new FrequencyPoint(
                        f.getPeriodStart(),
                        f.getFrequencyValue()
                ))
                .toList();

        // --- DAILY честоти вече НЯМА ---
        return new StockAnalysisResponse(
                masses,
                null,   // daily
                weekly,
                monthly,
                yearly
        );
    }
}
