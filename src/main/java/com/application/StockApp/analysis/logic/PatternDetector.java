package com.application.StockApp.analysis.logic;

import java.util.ArrayList;
import java.util.List;

public class PatternDetector {

    public record LHLPattern(
            SwingDetector.SwingPoint low1,
            SwingDetector.SwingPoint high,
            SwingDetector.SwingPoint low2
    ) {}

    public static List<LHLPattern> detectPatterns(List<SwingDetector.SwingPoint> swings) {

        List<LHLPattern> patterns = new ArrayList<>();

        for (int i = 0; i < swings.size() - 2; i++) {
            var s1 = swings.get(i);
            var s2 = swings.get(i + 1);
            var s3 = swings.get(i + 2);

            if (s1.type() == SwingDetector.Type.LOW &&
                    s2.type() == SwingDetector.Type.HIGH &&
                    s3.type() == SwingDetector.Type.LOW) {

                patterns.add(new LHLPattern(s1, s2, s3));
            }
        }

        return patterns;
    }
}
