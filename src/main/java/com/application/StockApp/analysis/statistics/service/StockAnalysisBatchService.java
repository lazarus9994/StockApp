package com.application.StockApp.analysis.statistics.service;

import com.application.StockApp.analysis.mathematics.service.StockDeltaService;
import com.application.StockApp.analysis.mathematics.repository.StockTriangleRepository;
import com.application.StockApp.analysis.physics.repository.StockFrequencyRepository;
import com.application.StockApp.analysis.physics.repository.StockMassRepository;
import com.application.StockApp.analysis.physics.service.*;
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
    private final StockDeltaService deltaService;
    private final StockFrequencyService frequencyService;
    private final StockAnalysisService analysisService;

    private final StockTriangleRepository triangleRepository;
    private final StockFrequencyRepository frequencyRepository;
    private final StockMassRepository massRepository;

    private final StockKineticsService kineticsService;
    private final StockTrajectoryService trajectoryService;
    private final StockOscillationService oscillationService;

    /**
     * Пълно изчистване на всички derived данни
     * и пресмятане наново за всички акции.
     */
    @Transactional
    public void rebuildAll() {

        System.out.println("🧹 Clearing old analysis data...");
        clearDerivedTables();

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

    /**
     * Старият режим – анализ без изчистване.
     */
    @Transactional
    public void analyzeAllStocksHistory() {
        stockRepository.findAll().forEach(stock -> {
            try {
                analyzeSafe(stock);
            } catch (Exception e) {
                System.err.println("⚠️ Failed: " + stock.getStockCode() + " → " + e.getMessage());
            }
        });
    }

    /**
     * Изпълнява пълния анализ върху една акция.
     */
    private void analyzeSafe(Stock stock) {

        System.out.println("▶ Starting analysis for " + stock.getStockCode());

        // 1) Масите по StockRecord
        massService.computeMasses(stock);

        // 2) Делти (price deltas)
        deltaService.computePriceDeltas(stock);

        // 3) Честоти и триъгълници
        frequencyService.computeAllFrequencies(stock);

        // 4) Кинетични стойности
        kineticsService.computeKinetics(stock);

        // 5) Траектории
        trajectoryService.computeTrajectories(stock);

        // 6) Осцилации
        oscillationService.computeWeeklyOscillations(stock);

        // 7) Summary (ако се използва)
        analysisService.buildSummary(stock);

        System.out.println("✅ Full analysis done for " + stock.getStockCode());
    }

    /**
     * Изчиства derived таблиците, без да пипа първични данни.
     */
    private void clearDerivedTables() {
        triangleRepository.deleteAll();
        frequencyRepository.deleteAll();
        massRepository.deleteAll();
        // Ако добавим delta / kinetics / trajectory таблици — тук.
    }
}
