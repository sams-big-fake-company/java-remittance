package com.bigfake.remittance;

import com.bigfake.remittance.util.ChecksumUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChecksumUtilsTest {
    @Test
    void computesStableSha256Hex() {
        assertEquals("2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b",
                ChecksumUtils.sha256("secret"));
    }
}
