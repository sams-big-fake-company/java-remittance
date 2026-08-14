package com.bigfake.remittance;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LegacyFixedWidthParserTest {

    @Test
    public void fixedWidthDetailUsesLegacyOffsets() {
        String record = fixedWidthRecord();

        assertEquals("PAYER00001", record.substring(1, 11).trim());
        assertEquals("INV-100045", record.substring(11, 31).trim());
        assertEquals("000000000000001250", record.substring(31, 49));
        assertEquals("000000000000001500", record.substring(49, 67));
        assertEquals("SHRT", record.substring(67, 71).trim());
        assertEquals("20260810", record.substring(71, 79));
    }

    private String fixedWidthRecord() {
        return String.format("D%-10s%-20s%018d%018d%-4s%-8s%-41s",
                "PAYER00001", "INV-100045", 1250, 1500, "SHRT", "20260810", "partial payment");
    }
}
