package com.application.StockApp.news.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "news")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stockCode;      // AAPL, MSFT etc.
    private String headline;
    private String content;
    private String source;
    private String url;

    @Column(unique = true)
    private String externalId;

    private LocalDateTime publishedAt;
    private LocalDateTime receivedAt;
}
