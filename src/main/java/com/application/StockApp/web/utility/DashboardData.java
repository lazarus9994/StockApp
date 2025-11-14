package com.application.StockApp.dashboard.service;

import com.application.StockApp.user.model.User;
import com.application.StockApp.watchlist.model.WatchlistItem;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record DashboardData(
        User user,
        List<WatchlistItem> watchlist,
        Map<String, Double> changes
) {
    public static DashboardData empty() {
        return new DashboardData(null, Collections.emptyList(), Collections.emptyMap());
    }

    public String firstSymbolOrDefault() {
        return watchlist.isEmpty() ? "NDAQ" : watchlist.get(0).getStock().getStockCode();
    }
}
