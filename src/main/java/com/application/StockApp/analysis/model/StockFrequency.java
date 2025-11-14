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
@Table(
        name = "stock_frequency",
        indexes = {
                @Index(name = "idx_freq_stock_date", columnList = "stock_id, date"),
                @Index(name = "idx_freq_stock_period", columnList = "stock_id, periodType")
        }
)
public class StockFrequency {

    public enum PeriodType {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PeriodType periodType;

    @Column(precision = 12, scale = 6, nullable = false)
    private BigDecimal frequency;
}
