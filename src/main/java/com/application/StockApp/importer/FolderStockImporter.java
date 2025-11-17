package com.application.StockApp.importer;

import com.application.StockApp.analysis.logic.IndexDetector;
import com.application.StockApp.analysis.service.StockAnalysisService;
import com.application.StockApp.analysis.service.StockFrequencyService;
import com.application.StockApp.analysis.service.StockMassService;
import com.application.StockApp.parser.CsvStockParser;
import com.application.StockApp.records.model.StockRecord;
import com.application.StockApp.records.repository.StockRecordRepository;
import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FolderStockImporter {

    @Value("${stock.data.folder}")
    private String stockDataFolder;

    private final CsvStockParser parser;
    private final StockRepository stockRepo;
    private final StockRecordRepository recordRepository;
    private final StockMassService massService;
    private final StockFrequencyService freqService;
    private final StockAnalysisService analysisService;

    @Transactional
    public void importAll() {
        File folder = new File(stockDataFolder);

        File[] files = folder.listFiles((f, n) -> n.endsWith(".csv"));
        if (files == null || files.length == 0) {
            System.out.println("⚠ No CSV files found.");
            return;
        }

        for (File file : files) {
            importSingle(file);
        }
    }

    @Transactional
    public void importSingle(File file) {
        String raw = file.getName().replace("-history.csv", "");
        String stockCode = raw.trim().toUpperCase();

        Stock stock = stockRepo.findByStockCode(stockCode)
                .orElseGet(() ->
                        stockRepo.save(Stock.builder()
                                .stockCode(stockCode)
                                .stockName(stockCode + " Stock")
                                .build())
                );

        // If data already exists → skip import fully
        boolean hasRecords = recordRepository.existsByStock(stock);
        if (hasRecords) {
            System.out.println("⏭ Already imported: " + stockCode);
            return;
        }

        try {
            List<StockRecord> records = parser.parseCsv(file.getAbsolutePath(), stock);

            recordRepository.saveAll(records);

            if (IndexDetector.isLikelyIndex(records)) {
                System.out.println("⛔ Skipping index: " + stockCode);
                return;
            }

            massService.computeMasses(stock);
            freqService.computeAllFrequencies(stock);
            analysisService.buildSummary(stock);

            System.out.println("✔ Imported + processed: " + stockCode);

        } catch (Exception e) {
            System.err.println("❌ Import failed for " + file.getName());
            e.printStackTrace();
        }
    }
}
