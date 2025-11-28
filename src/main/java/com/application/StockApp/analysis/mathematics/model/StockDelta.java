package com.application.StockApp.analysis.mathematics.model;

import com.application.StockApp.analysis.mathematics.model.MomentumDivergence;
import com.application.StockApp.analysis.mathematics.model.MomentumPattern;
import com.application.StockApp.stock.model.Stock;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDelta {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;

    @ManyToOne(optional = false)
    private Stock stock;

    @Column(nullable = false)
    private LocalDate date;

    @Column(precision = 20, scale = 6)
    private BigDecimal avgPricePrev;

    @Column(precision = 20, scale = 6)
    private BigDecimal avgPriceCurr;

    @Column(nullable = false, precision = 20, scale = 6)
    private BigDecimal delta;

    @Column(precision = 20, scale = 6)
    private BigDecimal delta2;

    @Column
    private Integer sign;

    @Column
    private Boolean signChange;

    @Column(precision = 20, scale = 6)
    private BigDecimal volatility;

    @Column(precision = 20, scale = 6)
    private BigDecimal deltaMomentum;

    @Column
    private Integer signMomentum;

    @Column(precision = 20, scale = 6)
    private BigDecimal emaMomentum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MomentumDivergence divergence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MomentumPattern momentumPattern;
}
