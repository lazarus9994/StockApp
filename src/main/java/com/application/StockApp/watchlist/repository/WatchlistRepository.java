package com.application.StockApp.watchlist.repository;

import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.watchlist.model.WatchlistItem;
import com.application.StockApp.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WatchlistRepository extends JpaRepository<WatchlistItem, UUID> {
    List<WatchlistItem> findAllByUser(User user);
    boolean existsByUserAndStock(User user, Stock stock);
    void deleteByUserAndStock(User user, Stock stock);

}
