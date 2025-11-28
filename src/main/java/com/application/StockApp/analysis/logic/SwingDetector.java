package com.application.StockApp.analysis.logic;

import com.application.StockApp.records.model.StockRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SwingDetector {

    public enum Type { HIGH, LOW }

    public record SwingPoint(LocalDate date, BigDecimal price, Type type) {}

    public static List<SwingPoint> detectSwings(List<StockRecord> records) {

        List<StockRecord> sorted = records.stream()
                .sorted(Comparator.comparing(StockRecord::getDate))
                .toList();

        List<SwingPoint> swings = new ArrayList<>();

        if (sorted.size() < 3) return swings;

        for (int i = 1; i < sorted.size() - 1; i++) {
            BigDecimal prev = sorted.get(i - 1).getClose();
            BigDecimal curr = sorted.get(i).getClose();
            BigDecimal next = sorted.get(i + 1).getClose();

            boolean isHigh = curr.compareTo(prev) > 0 && curr.compareTo(next) > 0;
            boolean isLow  = curr.compareTo(prev) < 0 && curr.compareTo(next) < 0;

            if (isHigh) swings.add(new SwingPoint(sorted.get(i).getDate(), curr, Type.HIGH));
            if (isLow)  swings.add(new SwingPoint(sorted.get(i).getDate(), curr, Type.LOW));
        }

        return swings;
    }
}
