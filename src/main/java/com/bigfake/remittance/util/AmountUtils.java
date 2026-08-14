package com.bigfake.remittance.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class AmountUtils {
    private AmountUtils() {
    }

    public static BigDecimal normalize(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(2)
                : value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal cents(long value) {
        return BigDecimal.valueOf(value, 2).setScale(2, RoundingMode.HALF_UP);
    }
}
