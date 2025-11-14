package com.application.StockApp.watchlist.service;

import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.records.repository.StockRecordRepository;
import com.application.StockApp.stock.repository.StockRepository;
import com.application.StockApp.user.model.User;
import com.application.StockApp.user.repository.UserRepository;
import com.application.StockApp.watchlist.model.WatchlistItem;
import com.application.StockApp.watchlist.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final StockRecordRepository recordRepository;

    private User getUser(String identifier) {
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + identifier));
    }


    public void addToWatchlist(Principal principal, Stock stock) {
        User user = getUser(principal.getName());

        boolean alreadyExists = watchlistRepository.findAllByUser(user).stream()
                .anyMatch(item -> item.getStock().equals(stock));
        if (alreadyExists) return;

        WatchlistItem item = WatchlistItem.builder()
                .user(user)
                .stock(stock)
                .note("") // или можеш да добавиш параметър
                .build();

        watchlistRepository.save(item);
    }

    public void addToWatchlist(Principal principal, String stockCode) {
        Stock stock = stockRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found: " + stockCode));
        addToWatchlist(principal, stock);
    }

    public void removeFromWatchlist(Principal principal, String code) {
        User user = getUser(principal.getName());
        watchlistRepository.findAllByUser(user).stream()
                .filter(item -> item.getStock().getStockCode().equals(code))

                .findFirst()
                .ifPresent(watchlistRepository::delete);
    }

    public List<WatchlistItem> getUserWatchlist(Principal principal) {
        User user = getUser(principal.getName());
        return watchlistRepository.findAllByUser(user);
    }

    public Map<String, Double> getWatchlistChangePercents(User user) {
        List<WatchlistItem> items = watchlistRepository.findAllByUser(user);
        Map<String, Double> changes = new HashMap<>();
        for (WatchlistItem item : items) {
            Double change = recordRepository.findLatestChangePercentByStock(item.getStock());
            changes.put(item.getStock().getStockCode(), change != null ? change : 0.0);
        }
        return changes;
    }

}
