package com.application.StockApp.analysis.statistics.service;

import com.application.StockApp.analysis.statistics.model.StockAnalysis;
import com.application.StockApp.analysis.physics.model.PeriodType;
import com.application.StockApp.analysis.statistics.repository.StockAnalysisRepository;
import com.application.StockApp.analysis.physics.repository.StockFrequencyRepository;
import com.application.StockApp.analysis.physics.repository.StockMassRepository;
import com.application.StockApp.stock.model.Stock;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StockAnalysisService {

    private final StockMassRepository massRepository;
    private final StockFrequencyRepository frequencyRepository;
    private final StockAnalysisRepository analysisRepository;

    @Transactional
    public void buildSummary(Stock stock) {
        StockAnalysis analysis = analysisRepository.findByStock(stock)
                .orElseGet(() -> StockAnalysis.builder()
                        .stock(stock)
                        .build());

        analysis.setAnalyzedAt(LocalDate.now());

        analysis.setAvgMass(
                massRepository.getAvgMass(stock.getId()).orElse(BigDecimal.ZERO));

        analysis.setAvgDailyFrequency(
                frequencyRepository.getAvgFrequency(stock.getId(), PeriodType.DAILY)
                        .orElse(BigDecimal.ZERO));

        analysis.setAvgWeeklyFrequency(
                frequencyRepository.getAvgFrequency(stock.getId(), PeriodType.WEEKLY)
                        .orElse(BigDecimal.ZERO));

        analysis.setAvgMonthlyFrequency(
                frequencyRepository.getAvgFrequency(stock.getId(), PeriodType.MONTHLY)
                        .orElse(BigDecimal.ZERO));

        analysis.setAvgYearlyFrequency(
                frequencyRepository.getAvgFrequency(stock.getId(), PeriodType.YEARLY)
                        .orElse(BigDecimal.ZERO));

        analysisRepository.save(analysis);

        System.out.println("✔ Summary saved for " + stock.getStockCode());
    }
}
