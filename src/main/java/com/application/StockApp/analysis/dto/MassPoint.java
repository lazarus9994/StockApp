package com.application.StockApp.analysis.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MassPoint(LocalDate date, BigDecimal mass) {}
