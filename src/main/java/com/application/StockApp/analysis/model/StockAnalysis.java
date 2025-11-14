package com.application.StockApp.analysis.model;

import com.application.StockApp.stock.model.Stock;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "stock_analysis")
public class StockAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    private LocalDate analyzedAt;

    // aggregated values
    @Column(precision = 20, scale = 6)
    private BigDecimal avgMass;

    @Column(precision = 20, scale = 6)
    private BigDecimal avgDailyFrequency;

    @Column(precision = 20, scale = 6)
    private BigDecimal avgWeeklyFrequency;

    @Column(precision = 20, scale = 6)
    private BigDecimal avgMonthlyFrequency;

    @Column(precision = 20, scale = 6)
    private BigDecimal avgYearlyFrequency;

    // basic prediction fields
    @Column(precision = 20, scale = 4)
    private BigDecimal forecastPrice;

    @Column(precision = 5, scale = 2)
    private BigDecimal confidence;
}
