package com.application.StockApp.analysis.physics.service;

import com.application.StockApp.analysis.physics.model.StockKinetics;
import com.application.StockApp.analysis.physics.model.StockMass;
import com.application.StockApp.analysis.physics.repository.StockKineticsRepository;
import com.application.StockApp.analysis.physics.repository.StockMassRepository;
import com.application.StockApp.records.model.StockRecord;
import com.application.StockApp.records.repository.StockRecordRepository;
import com.application.StockApp.stock.model.Stock;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockKineticsService {

    private final StockRecordRepository recordRepository;
    private final StockMassRepository massRepository;
    private final StockKineticsRepository kineticsRepository;

    @Transactional
    public void computeKinetics(Stock stock) {

        // чистим старите данни за тази акция
        kineticsRepository.deleteAllByStock(stock);

        var records = recordRepository.findAllByStock(stock).stream()
                .sorted(Comparator.comparing(StockRecord::getDate))
                .toList();

        if (records.size() < 3) {
            return;
        }

        // дневни маси: aggregated = false
        var dailyMassByDate = massRepository.findAllByStockOrderByDateAsc(stock).stream()
                .filter(m -> !Boolean.TRUE.equals(m.getAggregated()))
                .collect(Collectors.toMap(
                        StockMass::getDate,
                        java.util.function.Function.identity(),
                        (a, b) -> a
                ));

        List<StockKinetics> result = new ArrayList<>();

        for (int i = 2; i < records.size(); i++) {

            StockRecord r0 = records.get(i - 2);
            StockRecord r1 = records.get(i - 1);
            StockRecord r2 = records.get(i);

            LocalDate d0 = r0.getDate();
            LocalDate d1 = r1.getDate();
            LocalDate d2 = r2.getDate();

            BigDecimal p0 = averagePrice(r0);
            BigDecimal p1 = averagePrice(r1);
            BigDecimal p2 = averagePrice(r2);

            StockMass m1e = dailyMassByDate.get(d1);
            StockMass m2e = dailyMassByDate.get(d2);
            if (m1e == null || m2e == null) {
                continue;
            }

            BigDecimal m1 = m1e.getMass();
            BigDecimal m2 = m2e.getMass();
            if (m1 == null || m2 == null) {
                continue;
            }

            // velocity
            BigDecimal v1 = p1.subtract(p0).setScale(6, RoundingMode.HALF_UP);
            BigDecimal v2 = p2.subtract(p1).setScale(6, RoundingMode.HALF_UP);

            // acceleration = Δv
            BigDecimal a2 = v2.subtract(v1).setScale(6, RoundingMode.HALF_UP);

            // momentum = m * v
            BigDecimal q1 = m1.multiply(v1).setScale(6, RoundingMode.HALF_UP);
            BigDecimal q2 = m2.multiply(v2).setScale(6, RoundingMode.HALF_UP);

            // force = Δ(mv)
            BigDecimal F2 = q2.subtract(q1).setScale(6, RoundingMode.HALF_UP);

            StockKinetics k = StockKinetics.builder()
                    .stock(stock)
                    .date(d2)
                    .price(p2)
                    .mass(m2)
                    .velocity(v2)
                    .acceleration(a2)
                    .momentum(q2)
                    .netForce(F2)
                    .build();

            result.add(k);
        }

        kineticsRepository.saveAll(result);
    }

    private BigDecimal averagePrice(StockRecord r) {
        BigDecimal sum = BigDecimal.ZERO;
        int c = 0;

        if (r.getOpen() != null) { sum = sum.add(r.getOpen()); c++; }
        if (r.getHigh() != null) { sum = sum.add(r.getHigh()); c++; }
        if (r.getLow() != null)  { sum = sum.add(r.getLow());  c++; }
        if (r.getClose() != null){ sum = sum.add(r.getClose());c++; }

        if (c == 0) return BigDecimal.ZERO;

        return sum.divide(BigDecimal.valueOf(c), 6, RoundingMode.HALF_UP);
    }

    // --------- Пълна серия за кинетиката ---------
    public List<StockKinetics> getKinetics(Stock stock) {
        return kineticsRepository.findAllByStockOrderByDateAsc(stock);
    }

    public List<Map<String, Object>> getKineticsSeries(Stock stock) {

        return kineticsRepository.findAllByStockOrderByDateAsc(stock)
                .stream()
                .map(k -> Map.<String, Object>of(
                        "date", k.getDate().toString(),
                        "price", safe(k.getPrice()),
                        "velocity", safe(k.getVelocity()),
                        "acceleration", safe(k.getAcceleration()),
                        "mass", safe(k.getMass()),
                        "netForce", safe(k.getNetForce())
                ))
                .toList();
    }

    private BigDecimal safe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
