package com.application.StockApp.analysis.physics.repository;

import com.application.StockApp.analysis.physics.model.PeriodType;
import com.application.StockApp.analysis.physics.model.StockFrequency;
import com.application.StockApp.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockFrequencyRepository extends JpaRepository<StockFrequency, UUID> {

    List<StockFrequency> findAllByStock(Stock stock);

    List<StockFrequency> findAllByStockAndPeriodType(Stock stock, PeriodType type);

    void deleteAllByStock(Stock stock);

    @Query("SELECT AVG(f.frequency) FROM StockFrequency f " +
            "WHERE f.stock.id = :stockId AND f.periodType = :type")
    Optional<BigDecimal> getAvgFrequency(@Param("stockId") UUID stockId,
                                         @Param("type") PeriodType type);

}
