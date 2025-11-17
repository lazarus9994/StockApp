package com.application.StockApp.web.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewsDto {
    private String stockCode;
    private String headline;
    private String content;
    private String source;
    private String url;
    private String externalId;
    private LocalDateTime publishedAt;
}
