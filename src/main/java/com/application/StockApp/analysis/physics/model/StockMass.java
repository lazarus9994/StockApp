package com.application.StockApp.analysis.physics.model;

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
        name = "stock_mass",
        indexes = {
                @Index(name = "idx_stock_mass_stock_date", columnList = "stock_id, date"),
                @Index(name = "idx_stock_mass_stock_period", columnList = "stock_id, period_type, period_start")
        }
)
public class StockMass {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    /**
     * За дневните записи това е датата на масата.
     * За агрегирани записи може да съвпада с periodStart.
     */
    @Column(nullable = false)
    private LocalDate date;

    @Column(precision = 20, scale = 6, nullable = false)
    private BigDecimal mass;

    /**
     * null за дневните маси.
     * WEEKLY / MONTHLY / YEARLY – за агрегирани периоди.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "period_type")
    private PeriodType periodType;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    /**
     * false – дневен запис
     * true  – агрегирана маса за период
     */
    @Column(name = "is_aggregated")
    private Boolean aggregated;
}
