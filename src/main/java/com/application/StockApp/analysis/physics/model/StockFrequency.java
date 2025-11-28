package com.application.StockApp.analysis.physics.model;

import com.application.StockApp.analysis.physics.model.PeriodType;
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
                @Index(name = "idx_stock_freq_stock_period", columnList = "stock_id, period_type, period_start")
        }
)
public class StockFrequency {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false)
    private PeriodType periodType;  // WEEKLY / MONTHLY / YEARLY

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "cycle_count", nullable = false)
    private int cycleCount;

    @Column(name = "frequency_value", precision = 20, scale = 6)
    private BigDecimal frequencyValue;

    // 🔥 ново поле – маса за периода
    @Column(name = "mass", precision = 20, scale = 6)
    private BigDecimal mass;
}
