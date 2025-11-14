package com.application.StockApp.web;

import com.application.StockApp.stock.repository.StockRepository;
import com.application.StockApp.watchlist.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final StockRepository stockRepository;

    // 🟢 View all watchlist items
    @GetMapping
    public String viewWatchlist(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("watchlist", watchlistService.getUserWatchlist(principal));
        } else {
            model.addAttribute("watchlist", java.util.Collections.emptyList());
        }
        model.addAttribute("allStocks", stockRepository.findAll());
        return "watchlist";
    }

    // 🟢 Add stock to watchlist
    @PostMapping("/add")
    public String addToWatchlist(@RequestParam String code, Principal principal) {
        System.out.println("🟢 addToWatchlist called with " + code);
        watchlistService.addToWatchlist(principal, code);
        return "redirect:/watchlist?added=" + code;
    }

    // 🗑 Remove stock from watchlist
    @PostMapping("/remove")
    public String removeFromWatchlist(@RequestParam String code, Principal principal) {
        System.out.println("🟠 removeFromWatchlist called with " + code);
        watchlistService.removeFromWatchlist(principal, code);
        return "redirect:/watchlist?removed=" + code;
    }
}
