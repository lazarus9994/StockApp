package com.application.StockApp.analysis.physics.model;

import com.application.StockApp.stock.model.Stock;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "stock_kinetics",
        indexes = {
                @Index(name = "idx_kin_stock_date", columnList = "stock_id, date")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockKinetics {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Stock stock;

    @Column(nullable = false)
    private LocalDate date;

    // --- Основни стойности ---
    @Column(precision = 20, scale = 6)
    private BigDecimal price;        // средна цена за деня

    @Column(precision = 20, scale = 6)
    private BigDecimal mass;         // маса (вече я имаш от massService)

    // --- Физични derive стойности ---
    @Column(precision = 20, scale = 6)
    private BigDecimal velocity;     // ΔP

    @Column(precision = 20, scale = 6)
    private BigDecimal acceleration; // Δ(ΔP)

    @Column(precision = 20, scale = 6)
    private BigDecimal momentum;     // m * v

    @Column(precision = 20, scale = 6)
    private BigDecimal netForce;        // Δ(mv)
}
