package com.application.StockApp.stock.runner;

import com.application.StockApp.importer.FolderStockImporter;
import com.application.StockApp.stock.service.StockService;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CsvImportRunner implements CommandLineRunner {


    private final FolderStockImporter folderStockImporter;

    @Override
    public void run(String... args) throws Exception {
            // 🚀 Импорт на всички CSV файлове от папката, дефинирана в application.properties
            folderStockImporter.importAll();
    }
}
