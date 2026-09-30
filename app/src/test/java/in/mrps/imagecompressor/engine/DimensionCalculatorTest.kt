package `in`.mrps.imagecompressor.engine

import org.junit.Assert.*
import org.junit.Test

class DimensionCalculatorTest {
    @Test
    fun testOriginalDimensions() {
        val (w, h) = DimensionCalculator.calculateDimensions(100, 200, DimensionMode.Original, ScaleMode.FIT, true)
        assertEquals(100, w)
        assertEquals(200, h)
    }

    @Test
    fun testMaxDimensionsScaleDown() {
        val (w, h) = DimensionCalculator.calculateDimensions(1000, 2000, DimensionMode.MaxDimensions(500, 500), ScaleMode.FIT, true)
        assertEquals(250, w)
        assertEquals(500, h)
    }

    @Test
    fun testMaxDimensionsNoScaleUp() {
        val (w, h) = DimensionCalculator.calculateDimensions(100, 200, DimensionMode.MaxDimensions(500, 500), ScaleMode.FIT, true)
        assertEquals(100, w)
        assertEquals(200, h)
    }
    
    @Test
    fun testExactDimensionsLockRatio() {
        val (w, h) = DimensionCalculator.calculateDimensions(1000, 2000, DimensionMode.ExactDimensions(500, 500), ScaleMode.FIT, true)
        assertEquals(250, w)
        assertEquals(500, h)
    }

    @Test
    fun testExactDimensionsNoLock() {
        val (w, h) = DimensionCalculator.calculateDimensions(1000, 2000, DimensionMode.ExactDimensions(500, 500), ScaleMode.FIT, false)
        assertEquals(500, w)
        assertEquals(500, h)
    }
}
