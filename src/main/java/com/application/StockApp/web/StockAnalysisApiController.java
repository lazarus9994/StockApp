package com.application.StockApp.web;

import com.application.StockApp.analysis.dto.FrequencyPoint;
import com.application.StockApp.analysis.dto.MassPoint;
import com.application.StockApp.analysis.dto.StockAnalysisResponse;
import com.application.StockApp.analysis.physics.model.PeriodType;
import com.application.StockApp.analysis.physics.model.StockFrequency;
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

        var masses = massRepo.findAllByStock(stock)
                .stream()
                .map(m -> new MassPoint(m.getDate(), m.getMass()))
                .toList();

        var daily = freqRepo.findAllByStockAndPeriodType(stock, PeriodType.DAILY)
                .stream()
                .map(f -> new FrequencyPoint(f.getDate(), f.getFrequency()))
                .toList();

        var weekly = freqRepo.findAllByStockAndPeriodType(stock, PeriodType.WEEKLY)
                .stream()
                .map(f -> new FrequencyPoint(f.getDate(), f.getFrequency()))
                .toList();

        var monthly = freqRepo.findAllByStockAndPeriodType(stock, PeriodType.MONTHLY)
                .stream()
                .map(f -> new FrequencyPoint(f.getDate(), f.getFrequency()))
                .toList();

        var yearly = freqRepo.findAllByStockAndPeriodType(stock, PeriodType.YEARLY)
                .stream()
                .map(f -> new FrequencyPoint(f.getDate(), f.getFrequency()))
                .toList();

        return new StockAnalysisResponse(
                masses,
                daily,
                weekly,
                monthly,
                yearly
        );
    }
}
