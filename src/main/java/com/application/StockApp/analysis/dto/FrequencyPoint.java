package com.application.StockApp.analysis.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FrequencyPoint(LocalDate date, BigDecimal frequency) {}

