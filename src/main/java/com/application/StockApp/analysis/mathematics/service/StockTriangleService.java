package com.application.StockApp.analysis.mathematics.service;

import com.application.StockApp.analysis.logic.PatternDetector;
import com.application.StockApp.analysis.mathematics.model.StockTriangle;
import com.application.StockApp.analysis.mathematics.repository.StockTriangleRepository;
import com.application.StockApp.stock.model.Stock;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StockTriangleService {

    private final StockTriangleRepository triangleRepository;

    @Transactional
    public void computeTriangles(Stock stock,
                                 java.util.List<PatternDetector.LHLPattern> patterns) {

        // по желание: ако rebuildAll() вече прави deleteAll(), можеш да махнеш това
        triangleRepository.deleteAllByStock(stock);

        int index = 0;
        for (var p : patterns) {
            buildTriangle(stock, p, index++);
        }
    }

    private void buildTriangle(Stock stock, PatternDetector.LHLPattern p, int index) {

        var A = p.low1();
        var B = p.high();
        var C = p.low2();

        BigDecimal sideAB = distance(A.date(), A.price(), B.date(), B.price());
        BigDecimal sideBC = distance(B.date(), B.price(), C.date(), C.price());
        BigDecimal sideAC = distance(A.date(), A.price(), C.date(), C.price());

        BigDecimal angleA = angle(sideAB, sideAC, sideBC); // срещу BC
        BigDecimal angleB = angle(sideAB, sideBC, sideAC); // срещу AC
        BigDecimal angleC = angle(sideAC, sideBC, sideAB); // срещу AB

        BigDecimal amplitude = B.price()
                .subtract(A.price().min(C.price()))
                .max(BigDecimal.ZERO);

        long daysLong = C.date().toEpochDay() - A.date().toEpochDay();
        if (daysLong <= 0) {
            return;
        }

        BigDecimal periodDays = BigDecimal.valueOf(daysLong)
                .setScale(0, RoundingMode.HALF_UP); // ТИ ИСКАШ BigDecimal

        StockTriangle t = StockTriangle.builder()
                .stock(stock)
                .p1Date(A.date())
                .p2Date(B.date())
                .p3Date(C.date())
                .p1Price(A.price())
                .p2Price(B.price())
                .p3Price(C.price())
                .sideA(sideAB)
                .sideB(sideBC)
                .sideC(sideAC)
                .angleA(angleA)
                .angleB(angleB)
                .angleC(angleC)
                .amplitude(amplitude)
                .periodDays(periodDays)   // 👉 BigDecimal
                .patternType("LHL")
                .build();

        triangleRepository.save(t);
    }

    private BigDecimal distance(LocalDate d1, BigDecimal p1,
                                LocalDate d2, BigDecimal p2) {
        long x1 = d1.toEpochDay();
        long x2 = d2.toEpochDay();
        double dx = x2 - x1;
        double dy = p2.subtract(p1).doubleValue();
        double dist = Math.sqrt(dx * dx + dy * dy);
        return BigDecimal.valueOf(dist).setScale(6, RoundingMode.HALF_UP);
    }

    private BigDecimal angle(BigDecimal a, BigDecimal b, BigDecimal opposite) {
        double A = a.doubleValue();
        double B = b.doubleValue();
        double C = opposite.doubleValue();

        if (A == 0 || B == 0) {
            return BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);
        }

        double cos = (A*A + B*B - C*C) / (2 * A * B);
        if (cos > 1.0) cos = 1.0;
        if (cos < -1.0) cos = -1.0;

        double angleRad = Math.acos(cos);
        double angleDeg = Math.toDegrees(angleRad);

        return BigDecimal.valueOf(angleDeg).setScale(6, RoundingMode.HALF_UP);
    }
}
