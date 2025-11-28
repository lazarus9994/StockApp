package com.application.StockApp.analysis.physics.repository;

import com.application.StockApp.analysis.physics.model.PeriodType;
import com.application.StockApp.analysis.physics.model.StockMass;
import com.application.StockApp.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockMassRepository extends JpaRepository<StockMass, UUID> {

    List<StockMass> findAllByStockOrderByDateAsc(Stock stock);

    void deleteAllByStock(Stock stock);

    boolean existsByStockAndDate(Stock stock, LocalDate date);

    List<StockMass> findAllByStock(Stock stock);

    @Query("SELECT AVG(m.mass) FROM StockMass m WHERE m.stock.id = :stockId")
    Optional<BigDecimal> getAvgMass(@Param("stockId") UUID stockId);

    // 🔽 НОВИ МЕТОДИ ЗА АГРЕГИРАНИ ПЕРИОДИ 🔽

    List<StockMass> findAllByStockAndPeriodTypeOrderByPeriodStartAsc(
            Stock stock,
            PeriodType periodType
    );

    Optional<StockMass> findByStockAndPeriodTypeAndPeriodStart(
            Stock stock,
            PeriodType periodType,
            LocalDate periodStart
    );
}
