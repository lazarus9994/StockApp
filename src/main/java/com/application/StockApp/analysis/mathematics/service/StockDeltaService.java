package com.application.StockApp.analysis.mathematics.service;

import com.application.StockApp.analysis.mathematics.model.MomentumDivergence;
import com.application.StockApp.analysis.mathematics.model.MomentumPattern;
import com.application.StockApp.analysis.mathematics.model.StockDelta;
import com.application.StockApp.records.model.StockRecord;
import com.application.StockApp.records.repository.StockRecordRepository;
import com.application.StockApp.stock.model.Stock;
import com.application.StockApp.analysis.mathematics.repository.StockDeltaRepository;
import com.application.StockApp.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StockDeltaService {

    private final StockRecordRepository recordRepo;
    private final StockDeltaRepository deltaRepo;
    private final StockRepository stockRepository;
    private final StockRecordRepository recordRepository;

    private static final int EMA_PERIOD = 5;
    private static final BigDecimal EMA_ALPHA =
            BigDecimal.valueOf(2.0 / (EMA_PERIOD + 1));

    @Transactional
    public void computePriceDeltas(Stock stock) {

        deltaRepo.deleteAllByStock(stock);

        List<StockRecord> records = recordRepo.findAllByStock(stock).stream()
                .sorted(Comparator.comparing(StockRecord::getDate))
                .toList();

        if (records.size() < 3) return;

        BigDecimal prevAvgPrice = null;
        BigDecimal prevDelta = null;
        BigDecimal prevEmaMomentum = null;

        int signMomentumCounter = 0;
        int signPrev = 0;

        for (int i = 0; i < records.size(); i++) {

            StockRecord r = records.get(i);
            LocalDate date = r.getDate();

            BigDecimal avgPrice = averagePrice(r);

            if (prevAvgPrice == null) {
                prevAvgPrice = avgPrice;
                continue;
            }

            BigDecimal delta = avgPrice.subtract(prevAvgPrice)
                    .setScale(6, RoundingMode.HALF_UP);

            BigDecimal delta2 = null;
            if (prevDelta != null) {
                delta2 = delta.subtract(prevDelta)
                        .setScale(6, RoundingMode.HALF_UP);
            }

            int sign = delta.compareTo(BigDecimal.ZERO) > 0 ? 1 :
                    delta.compareTo(BigDecimal.ZERO) < 0 ? -1 : 0;

            boolean signChange = (signPrev != 0 && sign != signPrev);

            if (signMomentumCounter == 0) signMomentumCounter = sign;

            signMomentumCounter += sign;

            BigDecimal deltaMomentum = delta;

            // EMA
            BigDecimal emaMomentum;
            if (prevEmaMomentum == null) {
                emaMomentum = deltaMomentum;
            } else {
                emaMomentum =
                        deltaMomentum.multiply(EMA_ALPHA)
                                .add(prevEmaMomentum.multiply(BigDecimal.ONE.subtract(EMA_ALPHA)));
            }
            emaMomentum = emaMomentum.setScale(6, RoundingMode.HALF_UP);

            // Divergence
            MomentumDivergence divergence = MomentumDivergence.NONE;

            if (i >= 2) {
                BigDecimal prevPrice = records.get(i - 1).getClose();
                BigDecimal prevMomentum = prevDelta;

                if (prevPrice != null && prevMomentum != null) {
                    divergence = detectDivergence(
                            prevPrice,
                            avgPrice,
                            prevMomentum,
                            deltaMomentum
                    );
                }
            }

            // Pattern
            MomentumPattern pattern = detectPattern(
                    deltaMomentum,
                    signMomentumCounter,
                    emaMomentum,
                    signChange,
                    divergence
            );

            StockDelta d = StockDelta.builder()
                    .stock(stock)
                    .date(date)
                    .avgPricePrev(prevAvgPrice)
                    .avgPriceCurr(avgPrice)
                    .delta(delta)
                    .delta2(delta2)
                    .sign(sign)
                    .signChange(signChange)
                    .volatility(BigDecimal.ZERO)
                    .deltaMomentum(deltaMomentum)
                    .signMomentum(signMomentumCounter)
                    .emaMomentum(emaMomentum)
                    .divergence(divergence)
                    .momentumPattern(pattern)
                    .build();

            deltaRepo.save(d);

            prevEmaMomentum = emaMomentum;
            prevDelta = delta;
            prevAvgPrice = avgPrice;
            signPrev = sign;
        }
    }

    // ---------------------------
    //  Helpers
    // ---------------------------

    private BigDecimal averagePrice(StockRecord r) {
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;

        if (r.getOpen() != null) { sum = sum.add(r.getOpen()); count++; }
        if (r.getHigh() != null) { sum = sum.add(r.getHigh()); count++; }
        if (r.getLow() != null)  { sum = sum.add(r.getLow()); count++; }
        if (r.getClose() != null){ sum = sum.add(r.getClose()); count++; }

        if (count == 0) return BigDecimal.ZERO;

        return sum.divide(BigDecimal.valueOf(count), 6, RoundingMode.HALF_UP);
    }

    private MomentumDivergence detectDivergence(
            BigDecimal p1, BigDecimal p2, BigDecimal m1, BigDecimal m2
    ) {
        int pc = p2.compareTo(p1);
        int mc = m2.compareTo(m1);

        if (pc < 0 && mc > 0) return MomentumDivergence.BULLISH;
        if (pc > 0 && mc < 0) return MomentumDivergence.BEARISH;

        return MomentumDivergence.NONE;
    }

    private MomentumPattern detectPattern(
            BigDecimal deltaMomentum,
            Integer signMomentum,
            BigDecimal emaMomentum,
            boolean signChange,
            MomentumDivergence divergence
    ) {
        if (divergence != MomentumDivergence.NONE && signChange) {
            return MomentumPattern.DIVERGENCE_CONFIRM;
        }

        if (signChange && emaMomentum != null &&
                emaMomentum.abs().compareTo(BigDecimal.ZERO) > 0) {
            return MomentumPattern.REVERSAL;
        }

        if (deltaMomentum != null &&
                deltaMomentum.abs().compareTo(new BigDecimal("0.05")) < 0) {
            return MomentumPattern.SQUEEZE;
        }

        if (deltaMomentum != null &&
                deltaMomentum.abs().compareTo(new BigDecimal("1.0")) > 0 &&
                signMomentum != null &&
                Math.abs(signMomentum) > 7) {
            return MomentumPattern.EXPLOSION;
        }

        return MomentumPattern.NONE;
    }

    // --------- Raw price series за графиката ---------
    public List<Map<String, Object>> getRawPriceSeries(String stockCode) {

        Stock stock = stockRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new RuntimeException("Stock not found: " + stockCode));

        List<StockRecord> records = recordRepository.findAllByStockOrderByDateAsc(stock);

        List<Map<String, Object>> result = new ArrayList<>();

        for (StockRecord r : records) {
            Map<String, Object> point = new HashMap<>();
            point.put("date", r.getDate().toString());
            point.put("price", r.getClose());
            result.add(point);
        }

        return result;
    }

    // --------- Пълна серия от делти за дадена акция ---------
    public List<StockDelta> getDeltas(Stock stock) {
        return deltaRepo.findAllByStockOrderByDateAsc(stock);
    }

    public List<Map<String, Object>> getDeltaWindow(Stock stock, LocalDate from, LocalDate to) {

        return getDeltas(stock).stream()
                .filter(d -> !d.getDate().isBefore(from) && !d.getDate().isAfter(to))
                .map(d -> Map.<String, Object>of(
                        "date", d.getDate().toString(),
                        "delta", d.getDelta(),
                        "momentum", d.getMomentum(),
                        "acceleration", d.getAcceleration()
                ))
                .toList();
    }
}
