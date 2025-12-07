package com.application.StockApp.analysis.mathematics.repository;

import com.application.StockApp.analysis.mathematics.model.StockDelta;
import com.application.StockApp.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StockDeltaRepository extends JpaRepository<StockDelta, UUID> {

    List<StockDelta> findAllByStockOrderByDateAsc(Stock stock);

    void deleteAllByStock(Stock stock);

}
