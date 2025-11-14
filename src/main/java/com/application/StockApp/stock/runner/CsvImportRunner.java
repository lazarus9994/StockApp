package com.application.StockApp.stock.runner;

import com.application.StockApp.stock.service.StockService;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CsvImportRunner implements CommandLineRunner {

    private final StockService stockService;

    @Override
    public void run(String... args) throws Exception {
        try {
            // 🚀 Импорт на всички CSV файлове от папката, дефинирана в application.properties
            stockService.importAllFromFolder();
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }
}
