package com.application.StockApp.analysis.physics.service;

import com.application.StockApp.analysis.dto.MassPoint;
import com.application.StockApp.analysis.physics.model.StockMass;
import com.application.StockApp.analysis.physics.repository.StockMassRepository;
import com.application.StockApp.records.model.StockRecord;
import com.application.StockApp.records.repository.StockRecordRepository;
import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.analysis.physics.model.PeriodType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StockMassService {

    private final StockRecordRepository recordRepository;
    private final StockMassRepository massRepository;

    @Value("${stock.mass.scale:1000000000}")
    private BigDecimal k; // константа за "масата"

    @Transactional
    public void computeMasses(Stock stock) {


        List<StockRecord> records = recordRepository.findAllByStock(stock)
                .stream()
                .sorted(Comparator.comparing(StockRecord::getDate))
                .toList();

        for (StockRecord r : records) {
            LocalDate date = r.getDate();

            BigDecimal avgPrice = averagePrice(r);
            BigDecimal volume = r.getVolume() != null ? r.getVolume() : BigDecimal.ZERO;

            BigDecimal marketCap = avgPrice.multiply(volume); // проста капитализация за деня

            BigDecimal mass = marketCap.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : marketCap.divide(k, 6, RoundingMode.HALF_UP);

            StockMass massEntity = StockMass.builder()
                    .stock(stock)
                    .date(date)
                    .mass(mass)
                    .build();

            massRepository.save(massEntity);
        }
    }

    private BigDecimal averagePrice(StockRecord r) {
        // (open + high + low + close) / count(ненулеви)
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;

        if (r.getOpen() != null) { sum = sum.add(r.getOpen()); count++; }
        if (r.getHigh() != null) { sum = sum.add(r.getHigh()); count++; }
        if (r.getLow() != null) { sum = sum.add(r.getLow()); count++; }
        if (r.getClose() != null) { sum = sum.add(r.getClose()); count++; }

        if (count == 0) return BigDecimal.ZERO;
        return sum.divide(BigDecimal.valueOf(count), 6, RoundingMode.HALF_UP);
    }

    public List<MassPoint> getMassPoints(Stock stock) {
        return massRepository.findAllByStock(stock)
                .stream()
                .map(m -> new MassPoint(m.getDate(), m.getMass()))
                .toList();
    }

    public List<Map<String, Object>> getMassWindow(Stock stock, LocalDate from, LocalDate to) {

        return getMassPoints(stock).stream()
                .filter(p -> !p.date().isBefore(from) && !p.date().isAfter(to))
                .map(p -> Map.<String, Object>of(
                        "date", p.date().toString(),
                        "mass", p.mass()
                ))
                .toList();
    }

    @Transactional
    public void computePeriodMasses(Stock stock) {

        // 1) всички дневни маси (aggregated = false)
        var dailyMasses = massRepository.findAllByStockOrderByDateAsc(stock).stream()
                .filter(m -> !Boolean.TRUE.equals(m.getAggregated()))
                .toList();

        if (dailyMasses.isEmpty()) {
            return;
        }

        Map<LocalDate, List<StockMass>> weekly = new HashMap<>();
        Map<LocalDate, List<StockMass>> monthly = new HashMap<>();
        Map<LocalDate, List<StockMass>> yearly = new HashMap<>();

        for (StockMass m : dailyMasses) {
            LocalDate d = m.getDate();

            // седмица: понеделник–петък, ключ – понеделник
            LocalDate weekStart = d.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            weekly.computeIfAbsent(weekStart, k -> new ArrayList<>()).add(m);

            // месец: първо число
            LocalDate monthStart = d.withDayOfMonth(1);
            monthly.computeIfAbsent(monthStart, k -> new ArrayList<>()).add(m);

            // година: 1 януари
            LocalDate yearStart = LocalDate.of(d.getYear(), 1, 1);
            yearly.computeIfAbsent(yearStart, k -> new ArrayList<>()).add(m);
        }

        // 2) записваме агрегирани маси за всеки период
        saveAggregatedMasses(stock, weekly, PeriodType.WEEKLY);
        saveAggregatedMasses(stock, monthly, PeriodType.MONTHLY);
        saveAggregatedMasses(stock, yearly, PeriodType.YEARLY);
    }

    private void saveAggregatedMasses(
            Stock stock,
            Map<LocalDate, List<StockMass>> groups,
            PeriodType periodType
    ) {
        for (var entry : groups.entrySet()) {
            LocalDate periodStart = entry.getKey();
            List<StockMass> masses = entry.getValue();

            int days = masses.size();
            if (days == 0) continue;

            BigDecimal sum = masses.stream()
                    .map(StockMass::getMass)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // масата за периода: сума / брой дни
            BigDecimal avgMass = sum
                    .divide(BigDecimal.valueOf(days), 6, java.math.RoundingMode.HALF_UP);

            // date и periodStart ги държим еднакви за агрегирани записи
            LocalDate periodEnd;
            switch (periodType) {
                case WEEKLY -> periodEnd = periodStart.plusDays(4); // пон–пет
                case MONTHLY -> periodEnd = periodStart.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());
                case YEARLY -> periodEnd = periodStart.with(java.time.temporal.TemporalAdjusters.lastDayOfYear());
                default -> periodEnd = periodStart;
            }

            StockMass aggregated = massRepository
                    .findByStockAndPeriodTypeAndPeriodStart(stock, periodType, periodStart)
                    .orElseGet(StockMass::new);

            aggregated.setStock(stock);
            aggregated.setDate(periodStart);      // за агрегирани записи – начало на периода
            aggregated.setPeriodType(periodType);
            aggregated.setPeriodStart(periodStart);
            aggregated.setPeriodEnd(periodEnd);
            aggregated.setMass(avgMass);
            aggregated.setAggregated(true);

            massRepository.save(aggregated);
        }
    }
}
