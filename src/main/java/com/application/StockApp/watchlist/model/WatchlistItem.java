package com.application.StockApp.watchlist.model;

import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uc_user_stock_unique",
                columnNames = {"user_id", "stock_id"}
        )
)

public class WatchlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)

    private Stock stock;

    @Column
    private String note; // опционално — потребителска бележка
}
