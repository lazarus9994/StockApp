package com.application.StockApp.analysis.mathematics.repository;

import com.application.StockApp.analysis.mathematics.model.StockTriangle;
import com.application.StockApp.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StockTriangleRepository extends JpaRepository<StockTriangle, UUID> {

    List<StockTriangle> findAllByStock(Stock stock);
    void deleteAllByStock(Stock stock);

}
