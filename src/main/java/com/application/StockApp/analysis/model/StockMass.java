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
        name = "stock_mass",
        indexes = {
                @Index(name = "idx_mass_stock_date", columnList = "stock_id, date")
        }
)
public class StockMass {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private LocalDate date;

    @Column(precision = 20, scale = 6, nullable = false)
    private BigDecimal mass;
}
