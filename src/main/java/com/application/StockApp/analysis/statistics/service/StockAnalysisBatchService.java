package com.application.StockApp.analysis.statistics.service;

import com.application.StockApp.analysis.mathematics.repository.StockTriangleRepository;
import com.application.StockApp.analysis.mathematics.service.StockTriangleService;
import com.application.StockApp.analysis.physics.repository.StockFrequencyRepository;
import com.application.StockApp.analysis.physics.repository.StockMassRepository;
import com.application.StockApp.analysis.physics.service.StockFrequencyService;
import com.application.StockApp.analysis.physics.service.StockMassService;
import com.application.StockApp.records.model.StockRecord;
import com.application.StockApp.records.repository.StockRecordRepository;
import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.stock.repository.StockRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockAnalysisBatchService {

    private final StockRepository stockRepository;
    private final StockMassService massService;
    private final StockFrequencyService frequencyService;
    private final StockAnalysisService analysisService;
    private final StockTriangleService  triangleService;
    private final StockRecordRepository recordRepository;
    private final StockTriangleRepository triangleRepository;
    private final StockFrequencyRepository frequencyRepository;
    private final StockMassRepository massRepository;

    @Transactional
    public void rebuildAll() {

        // 1: Globally чистим таблиците
        //    (Може да използваме truncate през native query)
        //    Но по-безопасно — deleteAll()
        triangleRepository.deleteAll();
        frequencyRepository.deleteAll();
        massRepository.deleteAll();

        // 2: Пресмятаме за всяка акция
        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {

            var records = recordRepository.findAllByStock(stock)
                    .stream()
                    .sorted(Comparator.comparing(StockRecord::getDate))
                    .toList();

            massService.computeMasses(stock);

            frequencyService.computeAllFrequencies(stock);

            triangleService.computeTriangles(stock, records);
        }
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
        massService.computeMasses(stock);
        frequencyService.computeAllFrequencies(stock);
        analysisService.buildSummary(stock);
        System.out.println("✅ Full analysis done for " + stock.getStockCode());
    }
}
