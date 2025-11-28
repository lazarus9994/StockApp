package com.application.StockApp.web;

import com.application.StockApp.analysis.physics.model.PeriodType;
import com.application.StockApp.analysis.physics.service.StockFrequencyService;
import com.application.StockApp.analysis.physics.service.StockMassService;
import com.application.StockApp.records.model.StockRecord;
import com.application.StockApp.records.repository.StockRecordRepository;
import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.stock.repository.StockRepository;
import com.application.StockApp.stock.service.StockService;
import com.application.StockApp.web.dto.StockRecordDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockRestController {

    private final StockService stockService;
    private final StockMassService  stockMassService;
    private final StockRepository stockRepository;
    private final StockRecordRepository recordRepository;
    private final StockFrequencyService stockFrequencyService;

    @GetMapping("/codes")
    public List<String> getAllStockCodes() {
        return stockService.getAllStockCodes();
    }

    @GetMapping("/{code}/records-window")
    public List<StockRecordDto> getRecordsWindow(
            @PathVariable String code,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {

        Stock stock = stockRepository.findByStockCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException("Stock not found: " + code));

        List<StockRecord> records =
                recordRepository.findAllByStockAndDateBetweenOrderByDateAsc(stock, from, to);

        return records.stream()
                .map(StockRecord::toDTO)   // тук ползваме готовия toDTO()
                .toList();
    }

    @GetMapping("/{code}/mass-window")
    public List<Map<String, Object>> getMassWindow(
            @PathVariable String code,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        Stock stock = stockRepository.findByStockCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException("Stock not found: " + code));

        return stockMassService.getMassWindow(stock, from, to);
    }


    @GetMapping("/{code}/frequency/{type}")
    public List<Map<String, Object>> getFrequencyWindow(
            @PathVariable String code,
            @PathVariable String type,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {

        Stock stock = stockRepository.findByStockCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException("Stock not found: " + code));

        PeriodType period = switch (type.toLowerCase()) {
            case "daily" -> PeriodType.DAILY;
            case "weekly" -> PeriodType.WEEKLY;
            case "monthly" -> PeriodType.MONTHLY;
            case "yearly" -> PeriodType.YEARLY;
            default -> throw new RuntimeException("Invalid frequency type: " + type);
        };

        // Service already returns List<Map<String, Serializable>>
        return stockFrequencyService.getFrequencyWindow(stock, period, from, to);
    }

}
