package com.application.StockApp.analysis.physics.model;

import com.application.StockApp.stock.model.Stock;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "stock_trajectory",
        indexes = {
                @Index(name = "idx_traj_stock_date", columnList = "stock_id, date")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTrajectory {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Stock stock;

    @Column(nullable = false)
    private LocalDate date;

    // --- Предвидени стойности ---
    @Column(precision = 20, scale = 6)
    private BigDecimal predictedPrice;

    @Column(precision = 20, scale = 6)
    private BigDecimal predictedVelocity;

    // --- Сравнение с реалността ---
    @Column(precision = 20, scale = 6)
    private BigDecimal error;             // |realPrice - predictedPrice|

    // --- Физични показатели ---
    @Column(precision = 20, scale = 6)
    private BigDecimal inertiaIndex;      // m / F

    @Column(precision = 20, scale = 6)
    private BigDecimal forceEfficiency;   // F / m
}
