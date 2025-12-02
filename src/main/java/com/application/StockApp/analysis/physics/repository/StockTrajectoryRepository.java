package com.application.StockApp.analysis.physics.repository;

import com.application.StockApp.analysis.physics.model.StockTrajectory;
import com.application.StockApp.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StockTrajectoryRepository extends JpaRepository<StockTrajectory, UUID> {

    List<StockTrajectory> findAllByStockOrderByDateAsc(Stock stock);

    void deleteAllByStock(Stock stock);
}
