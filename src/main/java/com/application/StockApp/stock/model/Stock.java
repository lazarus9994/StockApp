package com.application.StockApp.stock.model;

import com.application.StockApp.records.model.StockRecord;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "stock",
        indexes = {
                @Index(name = "idx_stock_code", columnList = "stockCode"),
                @Index(name = "idx_stock_name", columnList = "stockName")
        }
)
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // --- Основна информация ---
    @Column(nullable = false, unique = true, length = 10)
    private String stockCode;

    @Column(nullable = false)
    private String stockName;

    @Column
    private String industry;

    // --- Финансови данни ---
    @Column(precision = 20, scale = 2)
    private BigDecimal sharesOutstanding; // общ брой акции в обращение

    @Column(precision = 20, scale = 2)
    private BigDecimal lastClosePrice; // последна затваряща цена

    @Column(precision = 20, scale = 4)
    private BigDecimal marketCap; // последна пазарна капитализация

    // --- Релации ---
    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private List<StockRecord> records = new ArrayList<>();

    // --- Метаданни ---
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Удобни методи ---
    public void updateMarketCap() {
        if (this.lastClosePrice != null && this.sharesOutstanding != null) {
            this.marketCap = this.lastClosePrice.multiply(this.sharesOutstanding);
        }
    }
}
