package com.application.StockApp.analysis.physics.service;

import com.application.StockApp.analysis.dto.FrequencyPoint;
import com.application.StockApp.analysis.logic.PatternDetector;
import com.application.StockApp.analysis.logic.SwingDetector;
import com.application.StockApp.analysis.mathematics.service.StockTriangleService;
import com.application.StockApp.analysis.physics.model.PeriodType;
import com.application.StockApp.analysis.physics.model.StockFrequency;
import com.application.StockApp.analysis.physics.model.StockMass;
import com.application.StockApp.analysis.physics.repository.StockFrequencyRepository;
import com.application.StockApp.analysis.physics.repository.StockMassRepository;
import com.application.StockApp.records.model.StockRecord;
import com.application.StockApp.records.repository.StockRecordRepository;
import com.application.StockApp.stock.model.Stock;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final StockMassRepository massRepository;

    @Transactional
    public void computeAllFrequencies(Stock stock) {

        // Изчистваме всички стари честоти за тази акция
        repo.deleteAllByStock(stock);

        // Зареждаме всички записи за цената, сортирани по дата
        var records = recordRepo.findAllByStock(stock).stream()
                .sorted(Comparator.comparing(StockRecord::getDate))
                .toList();

        if (records.size() < 3) {
            return;
        }

        // Детекваме swing точки (LOW / HIGH) и LHL pattern-и (min-max-min)
        var swings = SwingDetector.detectSwings(records);
        var patterns = PatternDetector.detectPatterns(swings);

        // Изчисляваме триъгълниците върху същите pattern-и (както досега)
        triangleService.computeTriangles(stock, patterns);

        // Подготвяме картите за брой pattern-и по период
        Map<LocalDate, Integer> weekly = new HashMap<>();
        Map<LocalDate, Integer> monthly = new HashMap<>();
        Map<LocalDate, Integer> yearly = new HashMap<>();

        // --- 1) седмични честоти ---
        for (var p : patterns) {
            // Честотата за даден pattern я отнасяме към седмицата на HIGH-а
            LocalDate d = p.high().date();

            LocalDate weekStart = d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            weekly.merge(weekStart, 1, Integer::sum);
        }

        // Зареждаме всички маси за акцията наведнъж, за да не правим много заявки
        List<StockMass> allMasses = massRepository.findAllByStockOrderByDateAsc(stock);

        // Съхраняваме седмичните честоти + маса
        repo.saveAll(build(stock, weekly, PeriodType.WEEKLY, allMasses));

        // --- 2) месечни честоти = сбор от седмичните ---
        for (var entry : weekly.entrySet()) {
            LocalDate week = entry.getKey();
            int count = entry.getValue();

            LocalDate monthStart = week.withDayOfMonth(1);
            monthly.merge(monthStart, count, Integer::sum);
        }

        repo.saveAll(build(stock, monthly, PeriodType.MONTHLY, allMasses));

        // --- 3) годишни честоти = сбор от месечните ---
        for (var entry : monthly.entrySet()) {
            LocalDate month = entry.getKey();
            int count = entry.getValue();

            LocalDate yearStart = month.withDayOfYear(1);
            yearly.merge(yearStart, count, Integer::sum);
        }

        repo.saveAll(build(stock, yearly, PeriodType.YEARLY, allMasses));
    }

    private List<StockFrequency> build(
            Stock stock,
            Map<LocalDate, Integer> map,
            PeriodType type,
            List<StockMass> allMasses
    ) {

        List<StockFrequency> list = new ArrayList<>();

        for (var e : map.entrySet()) {
            LocalDate periodStart = e.getKey();
            int cycleCount = e.getValue();

            LocalDate periodEnd;
            switch (type) {
                case WEEKLY -> periodEnd = periodStart.plusDays(4); // пон–пет
                case MONTHLY -> periodEnd = periodStart.with(TemporalAdjusters.lastDayOfMonth());
                case YEARLY -> periodEnd = periodStart.with(TemporalAdjusters.lastDayOfYear());
                default -> periodEnd = periodStart;
            }

            // Филтрираме масите, които попадат в този период
            List<StockMass> massesInPeriod = allMasses.stream()
                    .filter(m -> !m.getDate().isBefore(periodStart) && !m.getDate().isAfter(periodEnd))
                    .toList();

            BigDecimal avgMass = BigDecimal.ZERO;
            if (!massesInPeriod.isEmpty()) {
                BigDecimal sum = massesInPeriod.stream()
                        .map(StockMass::getMass)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                avgMass = sum.divide(
                        BigDecimal.valueOf(massesInPeriod.size()),
                        6,
                        RoundingMode.HALF_UP
                );
            }

            StockFrequency f = new StockFrequency();
            f.setStock(stock);
            f.setPeriodType(type);
            f.setPeriodStart(periodStart);
            f.setPeriodEnd(periodEnd);
            f.setCycleCount(cycleCount);
            f.setFrequencyValue(BigDecimal.valueOf(cycleCount));

            f.setMass(avgMass);

            list.add(f);
        }

        return list;
    }

    public List<FrequencyPoint> getFrequencyPoints(Stock stock, PeriodType type) {

        return frequencyRepository.findAllByStockAndPeriodType(stock, type).stream()
                .sorted(Comparator.comparing(StockFrequency::getPeriodStart))
                .map(f -> new FrequencyPoint(
                        f.getPeriodStart(),
                        f.getFrequencyValue()
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
