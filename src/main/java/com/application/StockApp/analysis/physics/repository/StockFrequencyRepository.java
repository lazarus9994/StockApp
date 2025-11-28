package com.application.StockApp.analysis.physics.repository;

import com.application.StockApp.analysis.physics.model.PeriodType;
import com.application.StockApp.analysis.physics.model.StockFrequency;
import com.application.StockApp.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockFrequencyRepository extends JpaRepository<StockFrequency, UUID> {

    List<StockFrequency> findAllByStock(Stock stock);

    List<StockFrequency> findAllByStockAndPeriodType(Stock stock, PeriodType type);

    Optional<StockFrequency> findByStockAndPeriodTypeAndPeriodStart(Stock stock, PeriodType periodType, LocalDate periodStart);

    List<StockFrequency> findAllByStockAndPeriodTypeOrderByPeriodStartAsc(Stock stock, PeriodType periodType);

    void deleteAllByStock(Stock stock);

    @Query("SELECT AVG(f.frequencyValue) FROM StockFrequency f WHERE f.stock.id = :stockId AND f.periodType = :type")
    Optional<BigDecimal> getAvgFrequency(UUID stockId, PeriodType type);



}
