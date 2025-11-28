package com.application.StockApp.analysis.physics.service;

import com.application.StockApp.analysis.dto.FrequencyPoint;
import com.application.StockApp.analysis.logic.PatternDetector;
import com.application.StockApp.analysis.logic.SwingDetector;
import com.application.StockApp.analysis.mathematics.service.StockTriangleService;
import com.application.StockApp.analysis.physics.model.PeriodType;
import com.application.StockApp.analysis.physics.model.StockFrequency;
import com.application.StockApp.analysis.physics.repository.StockFrequencyRepository;
import com.application.StockApp.records.model.StockRecord;
import com.application.StockApp.records.repository.StockRecordRepository;
import com.application.StockApp.stock.model.Stock;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StockFrequencyService {

    private final StockFrequencyRepository repo;
    private final StockRecordRepository recordRepo;
    private final StockFrequencyRepository frequencyRepository;
    private final StockTriangleService triangleService;

    @Transactional
    public void computeAllFrequencies(Stock stock) {

        repo.deleteAllByStock(stock);

        var records = recordRepo.findAllByStock(stock).stream()
                .sorted(Comparator.comparing(StockRecord::getDate))
                .toList();

        if (records.size() < 3) return;

        var swings = SwingDetector.detectSwings(records);
        var patterns = PatternDetector.detectPatterns(swings);

        Map<LocalDate, Integer> weekly = new HashMap<>();
        Map<LocalDate, Integer> monthly = new HashMap<>();
        Map<LocalDate, Integer> yearly = new HashMap<>();

        triangleService.computeTriangles(stock, patterns);

        // --- 1) седмични честоти ---
        for (var p : patterns) {
            // Честотата се дефинира на датата на HIGH
            LocalDate d = p.high().date();

            LocalDate weekStart = d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            weekly.merge(weekStart, 1, Integer::sum);
        }

        repo.saveAll(build(stock, weekly, PeriodType.WEEKLY));

        // --- 2) месечни честоти = сбор от седмичните ---
        for (var entry : weekly.entrySet()) {
            LocalDate week = entry.getKey();
            int count = entry.getValue();

            LocalDate monthStart = week.withDayOfMonth(1);
            monthly.merge(monthStart, count, Integer::sum);
        }

        repo.saveAll(build(stock, monthly, PeriodType.MONTHLY));

        // --- 3) годишни честоти = сбор от месечните ---
        for (var entry : monthly.entrySet()) {
            LocalDate month = entry.getKey();
            int count = entry.getValue();

            LocalDate yearStart = month.withDayOfYear(1);
            yearly.merge(yearStart, count, Integer::sum);
        }

        repo.saveAll(build(stock, yearly, PeriodType.YEARLY));
    }

    private List<StockFrequency> build(
            Stock stock,
            Map<LocalDate, Integer> map,
            PeriodType type
    ) {
        List<StockFrequency> list = new ArrayList<>();

        for (var e : map.entrySet()) {
            StockFrequency f = new StockFrequency();
            f.setStock(stock);
            f.setPeriodType(type);
            f.setDate(e.getKey());
            f.setFrequency(BigDecimal.valueOf(e.getValue()));
            list.add(f);
        }

        return list;
    }

    public List<FrequencyPoint> getFrequencyPoints(Stock stock, PeriodType type) {

        return frequencyRepository.findAllByStockAndPeriodType(stock, type).stream()
                .sorted(Comparator.comparing(StockFrequency::getDate))
                .map(f -> new FrequencyPoint(
                        f.getDate(),
                        f.getFrequency()      // BigDecimal или double
                ))
                .toList();
    }

    public List<Map<String, Object>> getFrequencyWindow(Stock stock, PeriodType period, LocalDate from, LocalDate to) {

        return getFrequencyPoints(stock, period).stream()
                .filter(p -> !p.date().isBefore(from) && !p.date().isAfter(to))
                .map(p -> Map.<String, Object>of(
                        "date", p.date().toString(),
                        "frequency", p.frequency()
                ))
                .toList();
    }

}
