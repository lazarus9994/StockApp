package com.application.StockApp.analysis.physics.service;

import com.application.StockApp.analysis.physics.model.StockKinetics;
import com.application.StockApp.analysis.physics.model.StockTrajectory;
import com.application.StockApp.analysis.physics.repository.StockKineticsRepository;
import com.application.StockApp.analysis.physics.repository.StockTrajectoryRepository;
import com.application.StockApp.stock.model.Stock;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockTrajectoryService {

    private final StockKineticsRepository kineticsRepository;
    private final StockTrajectoryRepository trajectoryRepository;

    @Transactional
    public void computeTrajectories(Stock stock) {

        trajectoryRepository.deleteAllByStock(stock);

        List<StockKinetics> kinetics = kineticsRepository
                .findAllByStockOrderByDateAsc(stock);

        if (kinetics.size() < 2) {
            return;
        }

        // just in case
        kinetics = kinetics.stream()
                .sorted(Comparator.comparing(StockKinetics::getDate))
                .toList();

        for (int i = 0; i < kinetics.size() - 1; i++) {

            StockKinetics kCurr = kinetics.get(i);
            StockKinetics kNext = kinetics.get(i + 1);

            BigDecimal priceCurr = safe(kCurr.getPrice());
            BigDecimal vCurr     = safe(kCurr.getVelocity());
            BigDecimal aCurr     = safe(kCurr.getAcceleration());
            BigDecimal massCurr  = safe(kCurr.getMass());
            BigDecimal forceCurr = safe(kCurr.getNetForce());

            // v_pred = v + a
            BigDecimal vPred = vCurr.add(aCurr)
                    .setScale(6, RoundingMode.HALF_UP);

            // p_pred = p + v_pred
            BigDecimal pPred = priceCurr.add(vPred)
                    .setScale(6, RoundingMode.HALF_UP);

            // real price = price of next kinetics point
            BigDecimal pRealNext = safe(kNext.getPrice());
            BigDecimal error = pRealNext.subtract(pPred).abs()
                    .setScale(6, RoundingMode.HALF_UP);

            // inertiaIndex = m / |F|
            BigDecimal inertiaIndex = null;
            BigDecimal absF = forceCurr.abs();
            if (massCurr.compareTo(BigDecimal.ZERO) != 0
                    && absF.compareTo(BigDecimal.ZERO) != 0) {
                inertiaIndex = massCurr.divide(absF, 6, RoundingMode.HALF_UP);
            }

            // forceEfficiency = |F| / m
            BigDecimal forceEfficiency = null;
            if (massCurr.compareTo(BigDecimal.ZERO) > 0) {
                forceEfficiency = absF.divide(massCurr, 6, RoundingMode.HALF_UP);
            }

            StockTrajectory t = StockTrajectory.builder()
                    .stock(stock)
                    .date(kNext.getDate())      // прогноза за "следващия" ден
                    .predictedPrice(pPred)
                    .predictedVelocity(vPred)
                    .error(error)
                    .inertiaIndex(inertiaIndex)
                    .forceEfficiency(forceEfficiency)
                    .build();

            trajectoryRepository.save(t);
        }
    }

    private BigDecimal safe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    public List<Map<String, Object>> getTrajectoryData(Stock stock) {

        return trajectoryRepository.findAllByStockOrderByDateAsc(stock)
                .stream()
                .map(t -> Map.<String, Object>of(
                        "date", t.getDate().toString(),
                        "predictedPrice", t.getPredictedPrice(),
                        "predictedVelocity", t.getPredictedVelocity(),
                        "error", t.getError(),
                        "inertiaIndex", t.getInertiaIndex(),
                        "forceEfficiency", t.getForceEfficiency()
                ))
                .toList();
    }

}
