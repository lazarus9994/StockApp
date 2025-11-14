package com.application.StockApp.analysis.service;

import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.stock.repository.StockRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockAnalysisBatchService {

    private final StockRepository stockRepository;
    private final StockMassService massService;
    private final StockFrequencyService frequencyService;
    private final StockAnalysisService analysisService;

    @Transactional
    public void analyzeAllStocksHistory() {
        stockRepository.findAll().forEach(stock -> {
            try {
                analyzeSafe(stock);
            } catch (Exception e) {
                System.err.println("⚠️ Failed for " + stock.getStockCode() + ": " + e.getMessage());
            }
        });
    }

    private void analyzeSafe(Stock stock) {
        massService.computeMasses(stock);
        frequencyService.computeAllFrequencies(stock);
        analysisService.buildSummary(stock);
        System.out.println("✅ Full analysis done for " + stock.getStockCode());
    }
}
