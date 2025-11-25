package com.application.StockApp.analysis.logic;

import com.application.StockApp.records.model.StockRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class SwingDetector {

    public record Pivot(LocalDate date, BigDecimal price, boolean isHigh, boolean isLow) {}

    public record SwingTriangle(
            LocalDate p1Date,
            LocalDate p2Date,
            LocalDate p3Date,
            BigDecimal p1Price,
            BigDecimal p2Price,
            BigDecimal p3Price,
            String patternType,
            BigDecimal periodDays,
            BigDecimal amplitude
    ) {}

    // ============================================================
    // PUBLIC METHOD — MAIN ENTRY POINT
    // ============================================================
    public static List<SwingTriangle> detectSwings(List<StockRecord> records) {

        if (records.size() < 10) return List.of();

        // Step 1: extract mid prices
        List<BigDecimal> prices = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();

        for (StockRecord r : records) {
            prices.add(mid(r));
            dates.add(r.getDate());
        }

        // Step 2: auto-tune parameters
        BigDecimal avgVolatility = estimateVolatility(prices);

        BigDecimal EPS = avgVolatility.multiply(BigDecimal.valueOf(2));
        if (EPS.compareTo(BigDecimal.valueOf(0.0001)) < 0)
            EPS = BigDecimal.valueOf(0.0001);

        int WINDOW = avgVolatility.compareTo(new BigDecimal("0.002")) > 0 ? 2 : 5;

        // Step 3: detect pivots
        List<Pivot> pivots = detectPivots(dates, prices, EPS, WINDOW);

        // Step 4: group pivots into triangles
        return buildTriangles(pivots);
    }

    // ============================================================
    // DETECT PIVOTS (highs and lows)
    // ============================================================
    private static List<Pivot> detectPivots(List<LocalDate> dates, List<BigDecimal> prices,
                                            BigDecimal EPS, int WINDOW) {

        List<Pivot> pivots = new ArrayList<>();
        int n = prices.size();

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
                pivots.add(new Pivot(
                        dates.get(i),
                        p,
                        isHigh,
                        isLow
                ));
            }
        }

        return pivots;
    }

    // ============================================================
    // FORM TRIANGLES (LHL + HLH)
    // ============================================================
    private static List<SwingTriangle> buildTriangles(List<Pivot> pivots) {

        List<SwingTriangle> swings = new ArrayList<>();

        // Need at least 3 pivot points
        for (int i = 0; i < pivots.size() - 2; i++) {

            Pivot p1 = pivots.get(i);
            Pivot p2 = pivots.get(i + 1);
            Pivot p3 = pivots.get(i + 2);

            String pattern = null;

            if (p1.isLow && p2.isHigh && p3.isLow)
                pattern = "LHL";

            if (p1.isHigh && p2.isLow && p3.isHigh)
                pattern = "HLH";

            if (pattern != null) {

                // period
                long days = ChronoUnit.DAYS.between(p1.date, p3.date);
                BigDecimal periodDays = BigDecimal.valueOf(days);

                // amplitude = height from p2 to base p1-p3
                BigDecimal amplitude = triangleHeight(
                        p1.date.toEpochDay(), p1.price,
                        p2.date.toEpochDay(), p2.price,
                        p3.date.toEpochDay(), p3.price
                );

                swings.add(new SwingTriangle(
                        p1.date, p2.date, p3.date,
                        p1.price, p2.price, p3.price,
                        pattern,
                        periodDays,
                        amplitude
                ));
            }
        }

        return swings;
    }

    // ============================================================
    // HEIGHT OF TRIANGLE (distance from p2 to line p1-p3)
    // ============================================================
    private static BigDecimal triangleHeight(long x1, BigDecimal y1,
                                             long x2, BigDecimal y2,
                                             long x3, BigDecimal y3) {

        BigDecimal X21 = BigDecimal.valueOf(x2 - x1);
        BigDecimal Y21 = y2.subtract(y1);

        BigDecimal X31 = BigDecimal.valueOf(x3 - x1);
        BigDecimal Y31 = y3.subtract(y1);

        BigDecimal numerator = X21.multiply(Y31).subtract(Y21.multiply(X31)).abs();

        BigDecimal denominator = BigDecimal.valueOf(
                Math.sqrt(Math.pow(x3 - x1, 2) + Math.pow(y3.subtract(y1).doubleValue(), 2))
        );

        if (denominator.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        return numerator.divide(denominator, 6, RoundingMode.HALF_UP);
    }

    // ============================================================
    // VOLATILITY ESTIMATION
    // ============================================================
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
