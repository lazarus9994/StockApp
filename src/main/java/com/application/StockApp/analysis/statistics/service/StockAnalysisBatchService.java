package com.application.StockApp.analysis.statistics.service;

import com.application.StockApp.analysis.mathematics.service.StockDeltaService;
import com.application.StockApp.analysis.mathematics.repository.StockTriangleRepository;
import com.application.StockApp.analysis.physics.repository.StockFrequencyRepository;
import com.application.StockApp.analysis.physics.repository.StockMassRepository;
import com.application.StockApp.analysis.physics.service.StockFrequencyService;
import com.application.StockApp.analysis.physics.service.StockMassService;
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
    private final StockDeltaService deltaService;     // <-- добавено
    private final StockFrequencyService frequencyService;
    private final StockAnalysisService analysisService;

    private final StockTriangleRepository triangleRepository;
    private final StockFrequencyRepository frequencyRepository;
    private final StockMassRepository massRepository;

    /**
     * Пълно изчистване на derived данните
     * и пресмятане наново за всички акции.
     */
    @Transactional
    public void rebuildAll() {

        System.out.println("🧹 Clearing old analysis data...");
        triangleRepository.deleteAll();
        frequencyRepository.deleteAll();
        massRepository.deleteAll();
        // При желание можем да изчистваме и delta таблицата тук
        // deltaRepository.deleteAll();

        System.out.println("📊 Rebuilding analysis for all stocks...");

        stockRepository.findAll().forEach(stock -> {
            try {
                analyzeSafe(stock);
            } catch (Exception e) {
                System.err.println("⚠️ Failed for " + stock.getStockCode() + ": " + e.getMessage());
            }
        });

        System.out.println("✅ Rebuild finished for all stocks.");
    }

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

        // 1) Масите по StockRecord
        massService.computeMasses(stock);

        // 2) Делтите по StockRecord
        deltaService.computePriceDeltas(stock);

        // 3) Честоти и триъгълници
        frequencyService.computeAllFrequencies(stock);

        // 4) Summary (ако го ползваш)
        analysisService.buildSummary(stock);

        System.out.println("✅ Full analysis done for " + stock.getStockCode());
    }
}
