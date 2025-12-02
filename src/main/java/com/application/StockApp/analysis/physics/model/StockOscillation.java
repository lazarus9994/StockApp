package com.application.StockApp.analysis.physics.model;

import com.application.StockApp.stock.model.Stock;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "stock_oscillation",
        indexes = {
                @Index(name = "idx_osc_stock_date", columnList = "stock_id, date")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockOscillation {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Stock stock;

    @Column(nullable = false)
    private LocalDate date;

    // --- Реална срещу теоретична честота ---
    @Column(precision = 20, scale = 6)
    private BigDecimal realFrequency;         // реална седмична/месечна честота

    @Column(precision = 20, scale = 6)
    private BigDecimal theoreticalFrequency;  // f = sqrt(k/m)

    // --- Параметри ---
    @Column(precision = 20, scale = 6)
    private BigDecimal kEffective;            // "spring constant" — изчислява се

    @Column(precision = 20, scale = 6)
    private BigDecimal deviation;             // |real - theoretical|
}
