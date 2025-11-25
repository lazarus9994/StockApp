package com.application.StockApp.analysis.mathematics.model;

import com.application.StockApp.stock.model.Stock;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "stock_triangle")
public class StockTriangle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    // --- Pivot dates ---
    private LocalDate p1Date;
    private LocalDate p2Date;
    private LocalDate p3Date;

    // --- Pivot prices ---
    @Column(precision = 19, scale = 6)
    private BigDecimal p1Price;

    @Column(precision = 19, scale = 6)
    private BigDecimal p2Price;

    @Column(precision = 19, scale = 6)
    private BigDecimal p3Price;

    // --- Core triangle parameters ---
    @Column(precision = 19, scale = 6)
    private BigDecimal amplitude;

    @Column(precision = 19, scale = 6)
    private BigDecimal periodDays;

    @Column(length = 10)
    private String patternType; // LHL, HLH

    // --- Triangle geometry ---
    @Column(precision = 19, scale = 6)
    private BigDecimal sideA;

    @Column(precision = 19, scale = 6)
    private BigDecimal sideB;

    @Column(precision = 19, scale = 6)
    private BigDecimal sideC;

    @Column(precision = 19, scale = 6)
    private BigDecimal angleA;

    @Column(precision = 19, scale = 6)
    private BigDecimal angleB;

    @Column(precision = 19, scale = 6)
    private BigDecimal angleC;
}
