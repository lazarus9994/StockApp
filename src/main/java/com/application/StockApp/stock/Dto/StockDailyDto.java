package com.application.StockApp.stock.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StockDailyDto(
        String stockCode,
        LocalDate date,
        BigDecimal close,
        double changePercent
) {}
