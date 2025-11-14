package com.application.StockApp.analysis.logic;

import com.application.StockApp.records.model.StockRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class SwingDetector {

    public record Swing(LocalDate fromDate, LocalDate toDate, long days) {}

    public static List<Swing> detectSwings(List<StockRecord> records) {
        if (records.size() < 3) return List.of();

        List<BigDecimal> prices = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();

        for (StockRecord r : records) {
            prices.add(mid(r));
            dates.add(r.getDate());
        }

        List<Swing> swings = new ArrayList<>();

        boolean rising = prices.get(1).compareTo(prices.get(0)) > 0;
        int swingStart = 0;

        for (int i = 1; i < prices.size() - 1; i++) {
            BigDecimal prev = prices.get(i - 1);
            BigDecimal curr = prices.get(i);
            BigDecimal next = prices.get(i + 1);

            boolean isTop = curr.compareTo(prev) > 0 && curr.compareTo(next) > 0;
            boolean isBottom = curr.compareTo(prev) < 0 && curr.compareTo(next) < 0;

            if (isTop || isBottom) {
                LocalDate start = dates.get(swingStart);
                LocalDate end = dates.get(i);
                long days = ChronoUnit.DAYS.between(start, end);

                if (days > 0) {
                    swings.add(new Swing(start, end, days));
                }

                swingStart = i;
                rising = !rising;
            }
        }

        return swings;
    }

    private static BigDecimal mid(StockRecord r) {
        if (r.getHigh() != null && r.getLow() != null) {
            return r.getHigh().add(r.getLow())
                    .divide(BigDecimal.valueOf(2), 6, RoundingMode.HALF_UP);
        }
        if (r.getClose() != null) return r.getClose();
        if (r.getOpen() != null) return r.getOpen();
        return BigDecimal.ZERO;
    }
}
