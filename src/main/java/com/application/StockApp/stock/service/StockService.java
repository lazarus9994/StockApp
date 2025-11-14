package com.application.StockApp.stock.service;

import com.application.StockApp.analysis.service.StockAnalysisService;
import com.application.StockApp.analysis.service.StockFrequencyService;
import com.application.StockApp.analysis.service.StockMassService;
import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.records.model.StockRecord;
import com.application.StockApp.records.repository.StockRecordRepository;
import com.application.StockApp.stock.repository.StockRepository;
import com.application.StockApp.web.dto.StockRecordDto;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.application.StockApp.utility.ParseUtils.*;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockRecordRepository recordRepository;
    private final StockAnalysisService stockAnalysisService;
    private final StockMassService  stockMassService;
    private final StockFrequencyService  stockFrequencyService;

    @Value("${stock.data.folder}")
    private String stockDataFolder;

    // ==============================================
    // 🧩 CSV IMPORT LOGIC
    // ==============================================
    @Transactional
    public void importCsv(String filePath) throws IOException, CsvValidationException {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {


            String[] nextLine;
            boolean firstLine = true;

            String fileName = new File(filePath).getName();
            String stockCode = fileName.replace("-history.csv", "").trim().toUpperCase();

            Stock stock = stockRepository.findByStockCode(stockCode)
                    .orElseGet(() -> {
                        System.out.println("📈 Creating new stock: " + stockCode);
                        Stock s = Stock.builder()
                                .stockCode(stockCode)
                                .stockName(stockCode + " Stock")
                                .build();
                        return stockRepository.save(s);
                    });

            while ((nextLine = reader.readNext()) != null) {

                if (firstLine) { firstLine = false; continue; }
                if (nextLine.length == 0 || nextLine[0].isBlank()) continue;

                LocalDate date = LocalDate.parse(nextLine[0]);
                BigDecimal open = parseDecimal(nextLine[1]);
                BigDecimal high = parseDecimal(nextLine[2]);
                BigDecimal low = parseDecimal(nextLine[3]);
                BigDecimal close = parseDecimal(nextLine[4]);
                BigDecimal adjClose = parseDecimal(nextLine[5]);
                BigDecimal changePercent = parseDecimal(nextLine[6].replace("%", ""));
                Long volume = parseLongSafe(nextLine[7]);

                // ⛔ Skip if record already exists for this stock and date
                if (recordRepository.existsByStockAndDate(stock, date)) {
                    continue;
                }


                StockRecord record = StockRecord.builder()
                        .stock(stock)
                        .date(date)
                        .open(open)
                        .high(high)
                        .low(low)
                        .close(close)
                        .adjustedClose(adjClose)
                        .volume(BigDecimal.valueOf(volume))
                        .changePercent(changePercent)
                        .build();

                recordRepository.save(record);
            }
            stockMassService.computeMasses(stock);
            stockFrequencyService.computeAllFrequencies(stock);
            stockAnalysisService.buildSummary(stock);

            System.out.println("✔ Fully processed " + stockCode);

        }
    }

    public void importAllFromFolder() throws IOException, CsvValidationException {
        File folder = new File(stockDataFolder);
        if (!folder.exists() || !folder.isDirectory())
            throw new IllegalArgumentException("Invalid directory: " + stockDataFolder);

        File[] csvFiles = folder.listFiles((dir, name) -> name.endsWith(".csv"));
        if (csvFiles == null || csvFiles.length == 0) {
            System.out.println("⚠️ No CSV files found in folder: " + stockDataFolder);
            return;
        }

        Arrays.sort(csvFiles);
        for (File file : csvFiles) {
            try {
                importCsv(file.getAbsolutePath());
                System.out.println("✅ Imported: " + file.getName());
            } catch (Exception e) {
                System.err.println("❌ Error importing " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ==============================================
    // ⚙️ AGGREGATED DATA VIA SQL (FAST)
    // ==============================================
    public List<StockRecordDto> getAggregatedStockData(String symbol, LocalDate start, LocalDate end, String range) {
        List<Object[]> rows;

        switch (range.toLowerCase()) {
            case "weekly" -> rows = recordRepository.aggregateWeeklyNative(symbol, start, end);
            case "monthly" -> rows = recordRepository.aggregateMonthlyNative(symbol, start, end);
            case "yearly" -> rows = recordRepository.aggregateYearlyNative(symbol, start, end);
            case "all" -> rows = recordRepository.aggregateAllNative(symbol, start, end);
            default -> rows = recordRepository.aggregateMonthlyNative(symbol, start, end);
        }

        return rows.stream()
                .map(this::mapRowToDto)
                .collect(Collectors.toList());
    }

    // ✅ Конвертор от SQL ред към DTO
    private StockRecordDto mapRowToDto(Object[] row) {
        return new StockRecordDto(
                ((java.sql.Date) row[0]).toLocalDate(),
                toBigDecimal(row[1]),
                toBigDecimal(row[2]),
                toBigDecimal(row[3]),
                toBigDecimal(row[4]),
                BigDecimal.ZERO,
                toBigDecimal(row[6])
        );
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal bd) return bd;
        return new BigDecimal(val.toString());
    }
}
