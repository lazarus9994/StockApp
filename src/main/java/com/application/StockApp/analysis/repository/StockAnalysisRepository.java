package com.application.StockApp.analysis.repository;

import com.application.StockApp.analysis.model.StockAnalysis;
import com.application.StockApp.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockAnalysisRepository extends JpaRepository<StockAnalysis, UUID> {

    List<StockAnalysis> findAllByStock(Stock stock);

    Optional<StockAnalysis> findByStock(Stock stock);
}
