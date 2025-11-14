package com.application.StockApp.records.repository;

import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.records.model.StockRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface StockRecordRepository extends JpaRepository<StockRecord, UUID> {
    List<StockRecord> findAllByStock(Stock stock);
    List<StockRecord> findAllByStockAndDateBetween(Stock stock, LocalDate startDate, LocalDate endDate);
    List<StockRecord> findAllByStockOrderByDateAsc(Stock stock);
    List<StockRecord> findAllByStockAndDateBetweenOrderByDateAsc(Stock stock, LocalDate start, LocalDate end);
    boolean existsByStockAndDate(Stock stock, LocalDate date);


    // --- 📅 Седмична агрегация (native SQL) ---
    @Query(value = """
        SELECT 
            MIN(r.date) AS date,
            AVG(r.open) AS open,
            AVG(r.high) AS high,
            AVG(r.low) AS low,
            AVG(r.close) AS close,
            0.0 AS change_percent,
            AVG(r.volume) AS volume
        FROM stock_record r
        JOIN stock s ON r.stock_id = s.id
        WHERE s.stock_code = :symbol
          AND r.date BETWEEN :start AND :end
        GROUP BY YEAR(r.date), WEEK(r.date)
        ORDER BY MIN(r.date)
        """, nativeQuery = true)
    List<Object[]> aggregateWeeklyNative(@Param("symbol") String symbol,
                                         @Param("start") LocalDate start,
                                         @Param("end") LocalDate end);

    // --- 🧮 Месечна агрегация ---
    @Query(value = """
        SELECT 
            MIN(r.date) AS date,
            AVG(r.open) AS open,
            AVG(r.high) AS high,
            AVG(r.low) AS low,
            AVG(r.close) AS close,
            0.0 AS change_percent,
            AVG(r.volume) AS volume
        FROM stock_record r
        JOIN stock s ON r.stock_id = s.id
        WHERE s.stock_code = :symbol
          AND r.date BETWEEN :start AND :end
        GROUP BY YEAR(r.date), MONTH(r.date)
        ORDER BY MIN(r.date)
        """, nativeQuery = true)
    List<Object[]> aggregateMonthlyNative(@Param("symbol") String symbol,
                                          @Param("start") LocalDate start,
                                          @Param("end") LocalDate end);


    // --- 📈 Годишна агрегация ---
    @Query(value = """
        SELECT 
            MIN(r.date) AS date,
            AVG(r.open) AS open,
            AVG(r.high) AS high,
            AVG(r.low) AS low,
            AVG(r.close) AS close,
            0.0 AS change_percent,
            AVG(r.volume) AS volume
        FROM stock_record r
        JOIN stock s ON r.stock_id = s.id
        WHERE s.stock_code = :symbol
          AND r.date BETWEEN :start AND :end
        GROUP BY YEAR(r.date)
        ORDER BY MIN(r.date)
        """, nativeQuery = true)
    List<Object[]> aggregateYearlyNative(@Param("symbol") String symbol,
                                         @Param("start") LocalDate start,
                                         @Param("end") LocalDate end);



    // --- 🗂️ Всички записи (без агрегация) ---
    @Query(value = """
    SELECT 
        r.date AS date,
        r.open AS open,
        r.high AS high,
        r.low AS low,
        r.close AS close,
        r.change_percent AS change_percent,
        r.volume AS volume
    FROM stock_record r
    JOIN stock s ON r.stock_id = s.id
    WHERE s.stock_code = :symbol
      AND r.date BETWEEN :start AND :end
    ORDER BY r.date
    """, nativeQuery = true)
    List<Object[]> aggregateAllNative(@Param("symbol") String symbol,
                                      @Param("start") LocalDate start,
                                      @Param("end") LocalDate end);

    @Query("""
    SELECT r.changePercent
    FROM StockRecord r
    WHERE r.stock = :stock
    ORDER BY r.date DESC
    LIMIT 1
""")
    Double findLatestChangePercentByStock(@Param("stock") Stock stock);



}
