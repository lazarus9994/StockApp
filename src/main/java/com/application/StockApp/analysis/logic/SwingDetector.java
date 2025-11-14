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

        if (records.size() < 10) return List.of();

        List<BigDecimal> prices = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();

        for (StockRecord r : records) {
            prices.add(mid(r));
            dates.add(r.getDate());
        }

        // =====================================================
        // 1) AUTO-TUNE PARAMETERS (EPS and WINDOW)
        // =====================================================
        BigDecimal avgVolatility = estimateVolatility(prices);

        // ако средната промяна е малка → EPS е супер малък
        BigDecimal EPS = avgVolatility.multiply(BigDecimal.valueOf(2));
        if (EPS.compareTo(BigDecimal.valueOf(0.0001)) < 0)
            EPS = BigDecimal.valueOf(0.0001);  // минимум 0.01%

        int WINDOW = avgVolatility.compareTo(new BigDecimal("0.002")) > 0 ? 2 : 5;

        System.out.println("AUTO EPS = " + EPS + ", WINDOW = " + WINDOW);

        // =====================================================
        // 2) Detect swings
        // =====================================================
        List<Swing> swings = new ArrayList<>();
        int n = prices.size();
        int lastSwingIndex = 0;

        for (int i = WINDOW; i < n - WINDOW; i++) {
            BigDecimal p = prices.get(i);

            boolean isHigh = true;
            boolean isLow = true;

            for (int k = 1; k <= WINDOW; k++) {
                BigDecimal prev = prices.get(i - k);
                BigDecimal next = prices.get(i + k);

                if (p.compareTo(prev.multiply(BigDecimal.ONE.add(EPS))) <= 0) isHigh = false;
                if (p.compareTo(next.multiply(BigDecimal.ONE.add(EPS))) <= 0) isHigh = false;

                if (p.compareTo(prev.multiply(BigDecimal.ONE.subtract(EPS))) >= 0) isLow = false;
                if (p.compareTo(next.multiply(BigDecimal.ONE.subtract(EPS))) >= 0) isLow = false;
            }

            if (isHigh || isLow) {
                LocalDate start = dates.get(lastSwingIndex);
                LocalDate end = dates.get(i);
                long days = ChronoUnit.DAYS.between(start, end);

                if (days > 0) swings.add(new Swing(start, end, days));

                lastSwingIndex = i;
            }
        }

        return swings;
    }

    private static BigDecimal estimateVolatility(List<BigDecimal> prices) {
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;

        for (int i = 1; i < prices.size(); i++) {
            BigDecimal p1 = prices.get(i - 1);
            BigDecimal p2 = prices.get(i);

            if (p1.compareTo(BigDecimal.ZERO) == 0) continue;

            BigDecimal change = p2.subtract(p1).abs().divide(p1, 6, RoundingMode.HALF_UP);
            total = total.add(change);
            count++;
        }

        if (count == 0) return BigDecimal.valueOf(0.001);

        return total.divide(BigDecimal.valueOf(count), 6, RoundingMode.HALF_UP);
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
