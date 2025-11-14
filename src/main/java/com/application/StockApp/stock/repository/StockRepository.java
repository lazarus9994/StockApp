package com.application.StockApp.stock.repository;

import com.application.StockApp.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface StockRepository extends JpaRepository<Stock, UUID> {
    Optional<Stock> findByStockCode(String stockCode);

    Optional<Stock> findByStockCodeIgnoreCase(String stockCode);
}


