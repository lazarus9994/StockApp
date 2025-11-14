package com.application.StockApp.records.model;

import com.application.StockApp.stock.model.Stock;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import com.application.StockApp.web.dto.StockRecordDto;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        indexes = {
                @Index(name = "idx_stock_date", columnList = "stock_id, date")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_stock_date_unique", columnNames = {"stock_id", "date"})
        }
)
public class StockRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    @JsonBackReference
    private Stock stock;

    @Column(nullable = false)
    private LocalDate date;

    @Column
    private BigDecimal open;

    @Column
    private BigDecimal high;

    @Column
    private BigDecimal low;

    @Column
    private BigDecimal close;

    @Column
    private BigDecimal adjustedClose;

    @Column
    private BigDecimal volume;

    @Column(precision = 10, scale = 4)
    private BigDecimal changePercent;


    public StockRecordDto toDTO() {
        return new StockRecordDto(
                this.date,
                this.open,
                this.high,
                this.low,
                this.close,
                this.changePercent,
                this.volume
        );
    }
}
