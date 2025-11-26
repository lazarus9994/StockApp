package com.application.StockApp.analysis.mathematics.service;

import com.application.StockApp.analysis.logic.SwingDetector;
import com.application.StockApp.analysis.mathematics.model.StockTriangle;
import com.application.StockApp.analysis.mathematics.repository.StockTriangleRepository;
import com.application.StockApp.records.model.StockRecord;
import com.application.StockApp.stock.model.Stock;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockTriangleService {

    private final StockTriangleRepository triangleRepository;

    @Transactional
    public void computeTriangles(Stock stock, List<StockRecord> records) {

        // 1) Изчистваме старите триъгълници за тази акция
        triangleRepository.deleteAllByStock(stock);

        // 2) Подреждаме записите по дата (за всеки случай)
        List<StockRecord> sorted = records.stream()
                .sorted(Comparator.comparing(StockRecord::getDate))
                .toList();

        // 3) Генерираме swing триъгълници
        List<SwingDetector.SwingTriangle> swings =
                SwingDetector.detectSwings(sorted);

        // 4) Превръщаме ги в StockTriangle и ги записваме
        buildTriangles(stock, swings);
    }

    public void buildTriangles(Stock stock, List<SwingDetector.SwingTriangle> swings) {

        for (SwingDetector.SwingTriangle sw : swings) {

            // --- Compute geometry ---
            BigDecimal sideA = distance(sw.p1Date(), sw.p1Price(), sw.p2Date(), sw.p2Price());
            BigDecimal sideB = distance(sw.p2Date(), sw.p2Price(), sw.p3Date(), sw.p3Price());
            BigDecimal sideC = distance(sw.p1Date(), sw.p1Price(), sw.p3Date(), sw.p3Price());

            BigDecimal angleA = angle(sideB, sideC, sideA);
            BigDecimal angleB = angle(sideA, sideC, sideB);
            BigDecimal angleC = angle(sideA, sideB, sideC);

            StockTriangle triangle = StockTriangle.builder()
                    .stock(stock)

                    .p1Date(sw.p1Date())
                    .p2Date(sw.p2Date())
                    .p3Date(sw.p3Date())

                    .p1Price(sw.p1Price())
                    .p2Price(sw.p2Price())
                    .p3Price(sw.p3Price())

                    .amplitude(sw.amplitude())
                    .periodDays(sw.periodDays())
                    .patternType(sw.patternType())

                    .sideA(sideA)
                    .sideB(sideB)
                    .sideC(sideC)

                    .angleA(angleA)
                    .angleB(angleB)
                    .angleC(angleC)

                    .build();

            triangleRepository.save(triangle);
        }
    }

    private BigDecimal distance(LocalDate d1, BigDecimal p1, LocalDate d2, BigDecimal p2) {
        long x1 = d1.toEpochDay();
        long x2 = d2.toEpochDay();
        double dx = x2 - x1;
        double dy = p2.subtract(p1).doubleValue();
        return BigDecimal.valueOf(Math.sqrt(dx * dx + dy * dy))
                .setScale(6, RoundingMode.HALF_UP);
    }

    private BigDecimal angle(BigDecimal a, BigDecimal b, BigDecimal opposite) {
        // Law of Cosines
        double A = a.doubleValue();
        double B = b.doubleValue();
        double C = opposite.doubleValue();

        double cos = (A*A + B*B - C*C) / (2 * A * B);
        double angle = Math.acos(cos);

        return BigDecimal.valueOf(Math.toDegrees(angle))
                .setScale(6, RoundingMode.HALF_UP);
    }

}
