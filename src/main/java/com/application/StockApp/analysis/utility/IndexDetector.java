package com.application.StockApp.analysis.logic;

import com.application.StockApp.records.model.StockRecord;

import java.math.BigDecimal;
import java.util.List;

public class IndexDetector {

    /**
     * Опитва се да определи дали dataset-ът е индекс:
     * индекси почти винаги растат монотонно (или с много малки колебания).
     */
    public static boolean isLikelyIndex(List<StockRecord> records) {
        if (records.size() < 10) return false;

        int monotonicUp = 0;
        int monotonicDown = 0;

        BigDecimal prev = mid(records.get(0));

        for (int i = 1; i < records.size(); i++) {
            BigDecimal curr = mid(records.get(i));

            int cmp = curr.compareTo(prev);

            if (cmp >= 0) monotonicUp++;
            if (cmp <= 0) monotonicDown++;

            prev = curr;
        }

        int n = records.size();

        // Ако повече от 95% са растящи: вероятно индекс
        if (monotonicUp > n * 0.95) return true;

        // Ако повече от 95% са падащи: пак индекс или "клинично зомби" :D
        if (monotonicDown > n * 0.95) return true;

        return false;
    }

    private static BigDecimal mid(StockRecord r) {
        if (r.getHigh() != null && r.getLow() != null)
            return r.getHigh().add(r.getLow()).divide(BigDecimal.valueOf(2));
        if (r.getClose() != null) return r.getClose();
        if (r.getOpen() != null) return r.getOpen();
        return BigDecimal.ZERO;
    }
}
