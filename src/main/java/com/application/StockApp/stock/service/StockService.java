package com.application.StockApp.stock.service;

import com.application.StockApp.records.repository.StockRecordRepository;
import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.stock.repository.StockRepository;
import com.application.StockApp.web.dto.StockRecordDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockRecordRepository recordRepository;

    // ==============================================
    // 📊 AGGREGATED DATA VIA NATIVE SQL (FAST)
    // ==============================================
    public List<StockRecordDto> getAggregatedStockData(String symbol,
                                                       LocalDate start,
                                                       LocalDate end,
                                                       String range) {

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


    // ==============================================
    // 🔄 Row → DTO mapping
    // ==============================================
    private StockRecordDto mapRowToDto(Object[] row) {
        return new StockRecordDto(
                ((java.sql.Date) row[0]).toLocalDate(),
                toBigDecimal(row[1]),
                toBigDecimal(row[2]),
                toBigDecimal(row[3]),
                toBigDecimal(row[4]),
                BigDecimal.ZERO,          // mass not used here
                toBigDecimal(row[6])      // volume or aggregated value
        );
    }


    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal bd) return bd;
        return new BigDecimal(val.toString());
    }

    public List<String> getAllStockCodes() {
        return stockRepository.findAll().stream().map(Stock::getStockCode).toList();
    }


}
