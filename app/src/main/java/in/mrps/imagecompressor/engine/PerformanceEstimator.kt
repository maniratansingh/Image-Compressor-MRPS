package `in`.mrps.imagecompressor.engine

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class PerformanceEstimator {
    private val msPerMegapixel = AtomicReference(50.0) // Default starting estimate

    fun recordOperation(pixels: Long, elapsedMs: Long) {
        if (pixels <= 0) return
        val megapixels = pixels / 1_000_000.0
        val currentRate = elapsedMs / megapixels
        val oldRate = msPerMegapixel.get()
        // Exponential moving average for smooth updates
        val newRate = (oldRate * 0.7) + (currentRate * 0.3)
        msPerMegapixel.set(newRate)
    }

    fun estimateRemainingMs(pixelsToProcess: Long): Long {
        if (pixelsToProcess <= 0) return 0
        val megapixels = pixelsToProcess / 1_000_000.0
        return (megapixels * msPerMegapixel.get()).toLong()
    }
}
