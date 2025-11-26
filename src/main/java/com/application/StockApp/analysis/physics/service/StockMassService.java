package com.application.StockApp.analysis.physics.service;

import com.application.StockApp.analysis.dto.MassPoint;
import com.application.StockApp.analysis.physics.model.StockMass;
import com.application.StockApp.analysis.physics.repository.StockMassRepository;
import com.application.StockApp.records.model.StockRecord;
import com.application.StockApp.records.repository.StockRecordRepository;
import com.application.StockApp.stock.model.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

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

}
