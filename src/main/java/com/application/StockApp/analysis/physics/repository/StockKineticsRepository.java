package com.application.StockApp.analysis.physics.repository;

import com.application.StockApp.analysis.physics.model.StockKinetics;
import com.application.StockApp.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StockKineticsRepository extends JpaRepository<StockKinetics, UUID> {

    List<StockKinetics> findAllByStockOrderByDateAsc(Stock stock);

    void deleteAllByStock(Stock stock);
}
