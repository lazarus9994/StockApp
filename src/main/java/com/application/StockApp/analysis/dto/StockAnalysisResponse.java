package com.application.StockApp.analysis.dto;

import java.util.List;

public record StockAnalysisResponse(
        List<MassPoint> masses,
        List<FrequencyPoint> daily,
        List<FrequencyPoint> weekly,
        List<FrequencyPoint> monthly,
        List<FrequencyPoint> yearly
) {}
