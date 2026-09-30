package `in`.mrps.imagecompressor.util

import org.junit.Assert.*
import org.junit.Test

class FormatUtilsTest {
    @Test
    fun testFormatPercentage() {
        assertEquals("50%", FormatUtils.formatPercentage(1000, 500))
    }

    @Test
    fun testFormatDimensions() {
        assertEquals("600 × 800 px", FormatUtils.formatDimensions(600, 800))
    }

    @Test
    fun testParseTargetSize() {
        assertEquals(204800L, FormatUtils.parseTargetSize("200", "KB"))
        assertEquals(2097152L, FormatUtils.parseTargetSize("2", "MB"))
        assertNull(FormatUtils.parseTargetSize("abc", "KB"))
    }
}
