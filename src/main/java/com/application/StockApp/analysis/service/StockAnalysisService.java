package com.application.StockApp.analysis.service;

import com.application.StockApp.analysis.model.StockAnalysis;
import com.application.StockApp.analysis.model.StockFrequency;
import com.application.StockApp.analysis.repository.StockAnalysisRepository;
import com.application.StockApp.analysis.repository.StockFrequencyRepository;
import com.application.StockApp.analysis.repository.StockMassRepository;
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
                frequencyRepository.getAvgFrequency(stock.getId(), StockFrequency.PeriodType.DAILY)
                        .orElse(BigDecimal.ZERO));

        analysis.setAvgWeeklyFrequency(
                frequencyRepository.getAvgFrequency(stock.getId(), StockFrequency.PeriodType.WEEKLY)
                        .orElse(BigDecimal.ZERO));

        analysis.setAvgMonthlyFrequency(
                frequencyRepository.getAvgFrequency(stock.getId(), StockFrequency.PeriodType.MONTHLY)
                        .orElse(BigDecimal.ZERO));

        analysis.setAvgYearlyFrequency(
                frequencyRepository.getAvgFrequency(stock.getId(), StockFrequency.PeriodType.YEARLY)
                        .orElse(BigDecimal.ZERO));

        analysisRepository.save(analysis);

        System.out.println("✔ Summary saved for " + stock.getStockCode());
    }
}
