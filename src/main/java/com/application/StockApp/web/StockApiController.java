package com.application.StockApp.web;

import com.application.StockApp.stock.service.StockService;
import com.application.StockApp.web.dto.StockRecordDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class StockApiController {

    private final StockService stockService;

    @GetMapping("/api/chart")
    @ResponseBody
    public List<StockRecordDto> getChartData(@RequestParam String range) {
        String symbol = "NDAQ";
        LocalDate end = LocalDate.now();
        LocalDate start = switch (range.toLowerCase()) {
            case "daily" -> end.minusWeeks(1);
            case "weekly" -> end.minusMonths(3);
            case "monthly" -> end.minusYears(1);
            case "yearly" -> end.minusYears(10);
            default -> LocalDate.of(1900, 1, 1);
        };
        return stockService.getAggregatedStockData(symbol, start, end, range);
    }

}
