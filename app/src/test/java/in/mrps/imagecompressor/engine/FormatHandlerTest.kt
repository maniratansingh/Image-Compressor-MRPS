package `in`.mrps.imagecompressor.engine

import org.junit.Assert.*
import org.junit.Test

class FormatHandlerTest {
    @Test
    fun testFileExtension() {
        assertEquals("jpg", FormatHandler.getFileExtension(OutputFormat.JPEG))
        assertEquals("png", FormatHandler.getFileExtension(OutputFormat.PNG))
        assertEquals("webp", FormatHandler.getFileExtension(OutputFormat.WEBP))
    }
    
    @Test
    fun testSupportsTransparency() {
        assertFalse(FormatHandler.supportsTransparency(OutputFormat.JPEG))
        assertTrue(FormatHandler.supportsTransparency(OutputFormat.PNG))
        assertTrue(FormatHandler.supportsTransparency(OutputFormat.WEBP))
    }
}
