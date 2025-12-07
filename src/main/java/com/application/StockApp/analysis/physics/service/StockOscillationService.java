package com.application.StockApp.analysis.physics.service;

import com.application.StockApp.analysis.physics.model.PeriodType;
import com.application.StockApp.analysis.physics.model.StockMass;
import com.application.StockApp.analysis.physics.model.StockOscillation;
import com.application.StockApp.analysis.physics.model.StockFrequency;
import com.application.StockApp.analysis.physics.repository.StockMassRepository;
import com.application.StockApp.analysis.physics.repository.StockOscillationRepository;
import com.application.StockApp.analysis.physics.repository.StockFrequencyRepository;
import com.application.StockApp.stock.model.Stock;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockOscillationService {

    private final StockMassRepository massRepository;
    private final StockFrequencyRepository frequencyRepository;
    private final StockOscillationRepository oscillationRepository;

    @Transactional
    public void computeWeeklyOscillations(Stock stock) {

        oscillationRepository.deleteAllByStock(stock);

        // агрегирани седмични маси
        var weeklyMass = massRepository.findAllByStockOrderByDateAsc(stock).stream()
                .filter(m -> Boolean.TRUE.equals(m.getAggregated()))
                .filter(m -> m.getPeriodType() == PeriodType.WEEKLY)
                .collect(Collectors.toMap(
                        StockMass::getPeriodStart,
                        Function.identity(),
                        (a, b) -> a
                ));

        if (weeklyMass.isEmpty()) return;

        // реални седмични честоти
        var frequencies = frequencyRepository
                .findAllByStockAndPeriodType(stock, PeriodType.WEEKLY);

        var freqByStart = frequencies.stream()
                .collect(Collectors.toMap(
                        StockFrequency::getPeriodStart,
                        Function.identity(),
                        (a, b) -> a
                ));

        // съвпадащи периоди
        record Pair(LocalDate start, StockMass m, StockFrequency f) {}
        List<Pair> pairs = new ArrayList<>();

        for (var e : weeklyMass.entrySet()) {
            LocalDate start = e.getKey();
            StockMass m = e.getValue();
            StockFrequency f = freqByStart.get(start);
            if (f != null) {
                pairs.add(new Pair(start, m, f));
            }
        }

        if (pairs.isEmpty()) return;

        // 1) оценка на k_eff
        double twoPi = 2.0 * Math.PI;
        double sumK = 0.0;
        int countK = 0;

        for (var p : pairs) {
            BigDecimal mass = p.m.getMass();
            BigDecimal fReal = p.f.getFrequencyValue();

            if (mass == null || fReal == null) continue;
            if (mass.compareTo(BigDecimal.ZERO) <= 0) continue;
            if (fReal.compareTo(BigDecimal.ZERO) <= 0) continue;

            double mVal = mass.doubleValue();
            double fVal = fReal.doubleValue();

            double k = Math.pow(twoPi * fVal, 2) * mVal;
            sumK += k;
            countK++;
        }

        if (countK == 0) return;

        double kEff = sumK / countK;
        BigDecimal kEffBD = BigDecimal.valueOf(kEff).setScale(6, RoundingMode.HALF_UP);

        // 2) теоретична честота + девиация
        for (var p : pairs) {
            LocalDate start = p.start;
            BigDecimal mass = p.m.getMass();
            BigDecimal fReal = p.f.getFrequencyValue();

            if (mass == null || fReal == null) continue;
            if (mass.compareTo(BigDecimal.ZERO) <= 0) continue;

            double mVal = mass.doubleValue();

            double fTheoVal = (1.0 / twoPi) * Math.sqrt(kEff / mVal);
            BigDecimal fTheo = BigDecimal.valueOf(fTheoVal)
                    .setScale(6, RoundingMode.HALF_UP);

            BigDecimal deviation = fReal.subtract(fTheo).abs()
                    .setScale(6, RoundingMode.HALF_UP);

            StockOscillation osc = StockOscillation.builder()
                    .stock(stock)
                    .date(start)
                    .realFrequency(fReal)
                    .theoreticalFrequency(fTheo)
                    .kEffective(kEffBD)
                    .deviation(deviation)
                    .build();

            oscillationRepository.save(osc);
        }
    }

    // --------- Пълни данни за осцилациите (за графика) ---------
    public List<Map<String, Object>> getOscillationData(Stock stock) {

        return oscillationRepository.findAllByStockOrderByDateAsc(stock).stream()
                .map(o -> Map.<String, Object>of(
                        "date", o.getDate().toString(),
                        "realFrequency", o.getRealFrequency(),
                        "theoreticalFrequency", o.getTheoreticalFrequency(),
                        "kEffective", o.getKEffective(),
                        "deviation", o.getDeviation()
                ))
                .toList();
    }

    public List<Map<String, Object>> getOscillationWindow(Stock stock, LocalDate from, LocalDate to) {

        return getOscillationData(stock).stream()
                .filter(o -> {
                    LocalDate d = LocalDate.parse(o.get("date").toString());
                    return !d.isBefore(from) && !d.isAfter(to);
                })
                .toList();
    }
}
