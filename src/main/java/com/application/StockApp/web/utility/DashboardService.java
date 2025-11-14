package com.application.StockApp.dashboard.service;

import com.application.StockApp.user.model.User;
import com.application.StockApp.user.service.UserService;
import com.application.StockApp.watchlist.model.WatchlistItem;
import com.application.StockApp.watchlist.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.application.StockApp.dashboard.service.DashboardData;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserService userService;
    private final WatchlistService watchlistService;

    public DashboardData loadDashboardData(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return DashboardData.empty();
        }

        try {
            User user = userService.getUser(principal.getName());
            List<WatchlistItem> watchlist = watchlistService.getUserWatchlist(principal);
            Map<String, Double> changes = watchlist.isEmpty()
                    ? Collections.emptyMap()
                    : watchlistService.getWatchlistChangePercents(user);

            return new DashboardData(user, watchlist, changes);
        } catch (Exception e) {
            System.err.println("⚠️ DashboardService: " + e.getMessage());
            return DashboardData.empty();
        }
    }
}
