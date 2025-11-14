package com.application.StockApp.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StockRecordDto(
        LocalDate date,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        BigDecimal change,
        BigDecimal volume
) {}
