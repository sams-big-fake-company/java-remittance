package com.bigfake.remittance;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LegacyFixedWidthParserTest {

    @Test
    public void fixedWidthDetailUsesLegacyOffsets() {
        String fixedWidthRecordValue = fixedWidthRecord();

        assertEquals("PAYER00001", fixedWidthRecordValue.substring(1, 11).trim());
        assertEquals("INV-100045", fixedWidthRecordValue.substring(11, 31).trim());
        assertEquals("000000000000001250", fixedWidthRecordValue.substring(31, 49));
        assertEquals("000000000000001500", fixedWidthRecordValue.substring(49, 67));
        assertEquals("SHRT", fixedWidthRecordValue.substring(67, 71).trim());
        assertEquals("20260810", fixedWidthRecordValue.substring(71, 79));
    }

    private String fixedWidthRecord() {
        return String.format("D%-10s%-20s%018d%018d%-4s%-8s%-41s",
                "PAYER00001", "INV-100045", 1250, 1500, "SHRT", "20260810", "partial payment");
    }
}
