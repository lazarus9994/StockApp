package com.application.StockApp.init;

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

    @EventListener(ApplicationReadyEvent.class)
    public void init() {

        long count = recordRepository.count();
        if (count > 0) {
            System.out.println("⏭ Stock records already loaded (" + count + ")");
            return;
        }

        System.out.println("🚀 Starting initial CSV import...");
        importer.importAll();
    }
}
