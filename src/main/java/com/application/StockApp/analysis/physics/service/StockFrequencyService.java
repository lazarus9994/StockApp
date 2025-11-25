package com.application.StockApp.analysis.physics.service;

import com.application.StockApp.analysis.logic.SwingDetector;
import com.application.StockApp.analysis.mathematics.service.StockTriangleService;
import com.application.StockApp.analysis.physics.model.PeriodType;
import com.application.StockApp.analysis.physics.model.StockFrequency;
import com.application.StockApp.analysis.physics.repository.StockFrequencyRepository;
import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.records.model.StockRecord;
import com.application.StockApp.records.repository.StockRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockFrequencyService {

    private final StockRecordRepository stockRecordRepository;
    private final StockFrequencyRepository frequencyRepository;
    private final StockTriangleService triangleService;

    // ============================================================
    // DAILY FREQUENCY (основният слой)
    // ============================================================
    public List<StockFrequency> computeDailyFrequencies(Stock stock) {

        // 1. Взимаме всички записи за акцията
        List<StockRecord> records = stockRecordRepository.findByStock(stock);

        // 2. Извличаме всички SwingTriangle (LHL + HLH)
        List<SwingDetector.SwingTriangle> swings = SwingDetector.detectSwings(records);

        // Записваме геометрията на триъгълниците
        triangleService.buildTriangles(stock, swings);


        // 3. Създаваме записи за всеки период
        List<StockFrequency> rawDaily = new ArrayList<>();

        for (SwingDetector.SwingTriangle sw : swings) {

            StockFrequency f = StockFrequency.builder()
                    .stock(stock)
                    .date(sw.p2Date())                   // централната точка (pivot)
                    .periodType(PeriodType.DAILY)
                    .periodDays(sw.periodDays())         // T
                    .amplitude(sw.amplitude())           // A
                    .patternType(sw.patternType())       // "LHL" или "HLH"
                    // временно frequency е null -> ще се попълни след групиране
                    .build();

            rawDaily.add(f);
        }

        // 4. Записваме първоначалните периоди (всеки SwingTriangle = един запис)
        List<StockFrequency> savedDaily = frequencyRepository.saveAll(rawDaily);

        // ============================================================
        // 5. Сега смятаме frequency(day) = count(periods for that date)
        // ============================================================
        Map<LocalDate, Long> counts = savedDaily.stream()
                .collect(Collectors.groupingBy(
                        StockFrequency::getDate,
                        Collectors.counting()
                ));

        // 6. Попълваме frequency за всеки дневен запис
        for (StockFrequency f : savedDaily) {
            long count = counts.getOrDefault(f.getDate(), 0L);

            // frequency = брой осцилации за деня
            f.setFrequency(BigDecimal.valueOf(count));
        }

        return frequencyRepository.saveAll(savedDaily);
    }

    // ============================================================
    // WEEKLY FREQUENCY
    // ============================================================
    public List<StockFrequency> computeWeeklyFrequencies(Stock stock) {

        List<StockFrequency> daily =
                frequencyRepository.findAllByStockAndPeriodType(stock, PeriodType.DAILY);

        var weeklyMap = daily.stream()
                .collect(Collectors.groupingBy(
                        f -> f.getDate().get(WeekFields.ISO.weekOfWeekBasedYear())
                ));

        List<StockFrequency> result = new ArrayList<>();

        for (var entry : weeklyMap.entrySet()) {

            BigDecimal sum = entry.getValue().stream()
                    .map(StockFrequency::getFrequency)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal avg = sum.divide(
                    BigDecimal.valueOf(entry.getValue().size()),
                    6, RoundingMode.HALF_UP
            );

            LocalDate representativeDate = entry.getValue().get(0).getDate();

            StockFrequency wf = StockFrequency.builder()
                    .stock(stock)
                    .date(representativeDate)
                    .periodType(PeriodType.WEEKLY)
                    .frequency(avg)
                    .build();

            result.add(wf);
        }

        return frequencyRepository.saveAll(result);
    }

    // ============================================================
    // MONTHLY FREQUENCY
    // ============================================================
    public List<StockFrequency> computeMonthlyFrequencies(Stock stock) {

        List<StockFrequency> daily =
                frequencyRepository.findAllByStockAndPeriodType(stock, PeriodType.DAILY);

        var monthlyMap = daily.stream()
                .collect(Collectors.groupingBy(f -> f.getDate().getMonthValue()));

        List<StockFrequency> result = new ArrayList<>();

        for (var entry : monthlyMap.entrySet()) {

            BigDecimal sum = entry.getValue().stream()
                    .map(StockFrequency::getFrequency)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal avg = sum.divide(
                    BigDecimal.valueOf(entry.getValue().size()),
                    6, RoundingMode.HALF_UP
            );

            LocalDate representativeDate = entry.getValue().get(0).getDate();

            StockFrequency mf = StockFrequency.builder()
                    .stock(stock)
                    .date(representativeDate)
                    .periodType(PeriodType.MONTHLY)
                    .frequency(avg)
                    .build();

            result.add(mf);
        }

        return frequencyRepository.saveAll(result);
    }

    // ============================================================
    // YEARLY FREQUENCY
    // ============================================================
    public List<StockFrequency> computeYearlyFrequencies(Stock stock) {

        List<StockFrequency> daily =
                frequencyRepository.findAllByStockAndPeriodType(stock, PeriodType.DAILY);

        var yearlyMap = daily.stream()
                .collect(Collectors.groupingBy(f -> f.getDate().getYear()));

        List<StockFrequency> result = new ArrayList<>();

        for (var entry : yearlyMap.entrySet()) {

            BigDecimal sum = entry.getValue().stream()
                    .map(StockFrequency::getFrequency)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal avg = sum.divide(
                    BigDecimal.valueOf(entry.getValue().size()),
                    6, RoundingMode.HALF_UP
            );

            LocalDate representativeDate = entry.getValue().get(0).getDate();

            StockFrequency yf = StockFrequency.builder()
                    .stock(stock)
                    .date(representativeDate)
                    .periodType(PeriodType.YEARLY)
                    .frequency(avg)
                    .build();

            result.add(yf);
        }

        return frequencyRepository.saveAll(result);
    }

    public void computeAllFrequencies(Stock stock) {
        computeDailyFrequencies(stock);
        computeWeeklyFrequencies(stock);
        computeMonthlyFrequencies(stock);
        computeYearlyFrequencies(stock);
    }

    public Object getFrequencyPoints(Stock stock, PeriodType periodType) {
        return frequencyRepository.findAllByStockAndPeriodType(stock, periodType);
    }
}
