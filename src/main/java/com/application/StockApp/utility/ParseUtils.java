package com.application.StockApp.utility;

import java.math.BigDecimal;

public class ParseUtils {

        private ParseUtils() {
            // предотвратява инстанциране
        }

        public static BigDecimal parseDecimal(String value) {
            if (value == null || value.isBlank()) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal(value.trim());
        }

        public static double parseDouble(String value) {
            if (value == null || value.isBlank()) {
                return 0.0;
            }
            return Double.parseDouble(value.replace("%", "").trim());
        }

        public static Long parseLongSafe(String value) {
            if (value == null || value.isBlank()) {
                return 0L;
            }
            return Long.parseLong(value.trim());
        }
    }