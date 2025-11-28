package com.application.StockApp.init;

import com.application.StockApp.analysis.statistics.service.StockAnalysisBatchService;
import com.application.StockApp.importer.FolderStockImporter;
import com.application.StockApp.records.repository.StockRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final FolderStockImporter importer;
    private final StockRecordRepository recordRepository;
    private final StockAnalysisBatchService batchService;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {

        long count = recordRepository.count();

        if (count == 0) {
            System.out.println("🚀 No records found, importing CSV...");
            importer.importAll();
        } else {
            System.out.println("⏭ Found " + count + " stock records.");
        }

        System.out.println("📊 Running full analysis rebuild...");
        batchService.rebuildAll();

        System.out.println("✅ Initial analysis completed.");
    }

}
