package com.application.StockApp.analysis.physics.model;

import com.application.StockApp.stock.model.Stock;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "stock_frequency")
public class StockFrequency {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    // Датата на средната точка (връх или дъно), както решихме
    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    // Честота за този ден (броят осцилации за деня)
    @Column(precision = 19, scale = 6)
    private BigDecimal frequency;

    // =======================================
    // Нови полета за същинския swing-период:
    // =======================================

    // Продължителност на периода (T)
    @Column(precision = 19, scale = 6)
    private BigDecimal periodDays;

    // Амплитуда (височина към основата на триъгълника)
    @Column(precision = 19, scale = 6)
    private BigDecimal amplitude;

    // Тип на pattern-а: "LHL" или "HLH"
    @Column(length = 10)
    private String patternType;
}
