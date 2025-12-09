package com.application.StockApp.web;

import com.application.StockApp.news.model.News;
import com.application.StockApp.news.service.NewsService;
import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.stock.repository.StockRepository;
import com.application.StockApp.watchlist.model.WatchlistItem;
import com.application.StockApp.watchlist.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final StockRepository stockRepository;
    private final NewsService newsService;
    private final WatchlistService watchlistService;

    @GetMapping("/")
    public String dashboard(
            Principal principal,
            Model model,
            @RequestParam(value = "range", defaultValue = "monthly") String range
    ) {

        Stock index = stockRepository.findByStockCode("NDAQ")
                .orElse(null);

        List<News> latest = newsService.getLatestNews();
        List<WatchlistItem> watchlist = watchlistService.getUserWatchlist(principal);

        model.addAttribute("index", index);
        model.addAttribute("selectedStock", "NDAQ");

        // Replace your old fixed "YEAR"
        model.addAttribute("selectedInterval", range.toUpperCase());

        // Needed for JS chart loader
        model.addAttribute("currentRange", range);

        model.addAttribute("news", latest);
        model.addAttribute("watchlist", watchlist);

        return "index";
    }
}
