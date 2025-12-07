package com.application.StockApp.web;

import com.application.StockApp.news.model.News;
import com.application.StockApp.news.service.NewsService;
import com.application.StockApp.records.model.StockRecord;
import com.application.StockApp.records.repository.StockRecordRepository;
import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.stock.repository.StockRepository;
import com.application.StockApp.watchlist.model.WatchlistItem;
import com.application.StockApp.watchlist.service.WatchlistService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final StockRepository stockRepository;
    private final StockRecordRepository recordRepository;
    private final NewsService newsService;
    private final WatchlistService watchlistService;

    // ------------------------
    // HTML DASHBOARD
    // ------------------------
    @GetMapping("/")
    public String dashboard(Principal principal, Model model) {

        Stock index = stockRepository.findByStockCode("NDAQ")
                .orElse(null);

        List<News> news = newsService.getLatestNews();
        List<WatchlistItem> watchlist = watchlistService.getUserWatchlist(principal);

        model.addAttribute("index", index);
        model.addAttribute("selectedInterval", "YEAR");
        model.addAttribute("selectedStock", "NDAQ");
        model.addAttribute("news", news);
        model.addAttribute("watchlist", watchlist);

        return "index";
    }

    // ------------------------
    // API: MAIN DASHBOARD CHART
    // ------------------------

    @GetMapping("/api/chart")
    @ResponseBody
    public ResponseEntity<?> getIndexChart(
            @RequestParam(defaultValue = "YEAR") String range
    ) {
        Stock index = stockRepository.findByStockCode("NDAQ")
                .orElseThrow(() -> new IllegalArgumentException("Missing NDAQ"));

        LocalDate now = LocalDate.now();
        LocalDate from = switch (range) {
            case "WEEK" -> now.minusWeeks(1);
            case "MONTH" -> now.minusMonths(1);
            case "YEAR" -> now.minusYears(1);
            default -> now.minusYears(50);
        };

        var records = stockRecordRepository.findAllByStockAndDateBetweenOrderByDateAsc(index, from, now);

        return ResponseEntity.ok(records.stream()
                .map(r -> Map.of(
                        "date", r.getDate().toString(),
                        "close", r.getClose()
                )).toList());
    }
}
