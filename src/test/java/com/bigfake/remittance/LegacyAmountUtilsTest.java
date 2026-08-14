package com.bigfake.remittance;

import com.bigfake.remittance.util.AmountUtils;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
public class LegacyAmountUtilsTest {
 @Test public void normalizesUsingHalfUp(){assertEquals(new BigDecimal("10.13"),AmountUtils.normalize(new BigDecimal("10.125")));}
 @Test public void convertsCents(){assertEquals(new BigDecimal("12.50"),AmountUtils.cents(1250));}
}
