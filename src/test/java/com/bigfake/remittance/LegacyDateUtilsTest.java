package com.bigfake.remittance;

import com.bigfake.remittance.util.DateUtils;
import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
public class LegacyDateUtilsTest {
 @Test public void parsesFixedWidthDate() throws Exception {assertNotNull(DateUtils.parseLegacy("20260810"));}
 @Test(expected=ParseException.class) public void rejectsInvalidDate() throws Exception {DateUtils.parseLegacy("20261340");}
}
