package com.application.StockApp.analysis.service;

import com.application.StockApp.analysis.logic.SwingDetector;
import com.application.StockApp.analysis.logic.SwingDetector.Swing;
import com.application.StockApp.analysis.model.StockFrequency;
import com.application.StockApp.analysis.model.StockFrequency.PeriodType;
import com.application.StockApp.analysis.repository.StockFrequencyRepository;
import com.application.StockApp.records.model.StockRecord;
import com.application.StockApp.records.repository.StockRecordRepository;
import com.application.StockApp.stock.model.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StockFrequencyService {

    private final StockRecordRepository recordRepository;
    private final StockFrequencyRepository frequencyRepository;

    @Transactional
    public void computeAllFrequencies(Stock stock) {

        // 1) чистим старите честоти
        frequencyRepository.deleteAllByStock(stock);

        // 2) взимаме всички записи
        List<StockRecord> records = recordRepository.findAllByStock(stock)
                .stream()
                .sorted(Comparator.comparing(StockRecord::getDate))
                .toList();

        if (records.size() < 3) {
            System.out.println("⚠️ Not enough records for frequency: " + stock.getStockCode());
            return;
        }

        // 3) намираме swing движения (обща логика)
        List<Swing> swings = SwingDetector.detectSwings(records);

        if (swings.isEmpty()) {
            System.out.println("⚠️ No swings detected for: " + stock.getStockCode());
            return;
        }

        // 4) DAILY честота
        saveFrequencies(stock, swings, PeriodType.DAILY);

        // 5) WEEKLY
        saveGroupedFrequencies(stock, swings, PeriodType.WEEKLY, 7);

        // 6) MONTHLY
        saveMonthlyFrequencies(stock, swings);

        // 7) YEARLY
        saveYearlyFrequencies(stock, swings);

        System.out.println("✔ Frequencies saved for: " + stock.getStockCode());
    }

    // ============================================
    // DAILY — директно използваме swing периода
    // ============================================
    private void saveFrequencies(Stock stock, List<Swing> swings, PeriodType type) {
        for (Swing s : swings) {
            BigDecimal freq = BigDecimal.ONE
                    .divide(BigDecimal.valueOf(s.days()), 6, java.math.RoundingMode.HALF_UP);

            frequencyRepository.save(
                    StockFrequency.builder()
                            .stock(stock)
                            .date(s.toDate()) // датата на минимума/края
                            .periodType(type)
                            .frequency(freq)
                            .build()
            );
        }
    }

    // ============================================
    // WEEKLY — групиране на swing по 7 дни
    // ============================================
    private void saveGroupedFrequencies(Stock stock,
                                        List<Swing> swings,
                                        PeriodType type,
                                        int groupDays) {

        Map<Integer, List<Swing>> buckets = new HashMap<>();

        for (Swing s : swings) {
            int bucket = (int) (s.days() / groupDays);
            buckets.computeIfAbsent(bucket, b -> new ArrayList<>()).add(s);
        }

        for (List<Swing> group : buckets.values()) {
            long avgDays = (long) group.stream().mapToLong(Swing::days).average().orElse(0);

            if (avgDays <= 0) continue;

            BigDecimal freq = BigDecimal.ONE
                    .divide(BigDecimal.valueOf(avgDays), 6, java.math.RoundingMode.HALF_UP);

            frequencyRepository.save(
                    StockFrequency.builder()
                            .stock(stock)
                            .date(group.get(group.size() - 1).toDate())
                            .periodType(type)
                            .frequency(freq)
                            .build()
            );
        }
    }

    // ============================================
    // MONTHLY — групиране по календарни месеци
    // ============================================
    private void saveMonthlyFrequencies(Stock stock, List<Swing> swings) {
        Map<String, List<Swing>> buckets = new HashMap<>();

        for (Swing s : swings) {
            String key = s.toDate().getYear() + "-" + s.toDate().getMonthValue();
            buckets.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
        }

        for (List<Swing> group : buckets.values()) {
            long avgDays = (long) group.stream().mapToLong(Swing::days).average().orElse(0);

            if (avgDays <= 0) continue;

            BigDecimal freq = BigDecimal.ONE
                    .divide(BigDecimal.valueOf(avgDays), 6, java.math.RoundingMode.HALF_UP);

            LocalDate lastDate = group.get(group.size() - 1).toDate();

            frequencyRepository.save(
                    StockFrequency.builder()
                            .stock(stock)
                            .date(lastDate)
                            .periodType(PeriodType.MONTHLY)
                            .frequency(freq)
                            .build()
            );
        }
    }

    // ============================================
    // YEARLY — групиране по години
    // ============================================
    private void saveYearlyFrequencies(Stock stock, List<Swing> swings) {
        Map<Integer, List<Swing>> buckets = new HashMap<>();

        for (Swing s : swings) {
            int year = s.toDate().getYear();
            buckets.computeIfAbsent(year, k -> new ArrayList<>()).add(s);
        }

        for (List<Swing> group : buckets.values()) {
            long avgDays = (long) group.stream().mapToLong(Swing::days).average().orElse(0);

            if (avgDays <= 0) continue;

            BigDecimal freq = BigDecimal.ONE
                    .divide(BigDecimal.valueOf(avgDays), 6, java.math.RoundingMode.HALF_UP);

            LocalDate lastDate = group.get(group.size() - 1).toDate();

            frequencyRepository.save(
                    StockFrequency.builder()
                            .stock(stock)
                            .date(lastDate)
                            .periodType(PeriodType.YEARLY)
                            .frequency(freq)
                            .build()
            );
        }
    }
}
