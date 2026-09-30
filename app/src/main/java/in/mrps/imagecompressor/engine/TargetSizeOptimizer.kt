package `in`.mrps.imagecompressor.engine

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.yield

data class OptimizationResult(
    val outputBytes: ByteArray,
    val quality: Int,
    val width: Int,
    val height: Int,
    val qualityWarning: Boolean,
    val actualSizeBytes: Long
)

class TargetSizeOptimizer {
    suspend fun optimize(
        bitmap: Bitmap,
        targetSizeBytes: Long,
        format: OutputFormat,
        onStepUpdate: (ProcessingStep) -> Unit
    ): OptimizationResult {
        onStepUpdate(ProcessingStep.QUALITY)
        
        val target = (targetSizeBytes * 0.98).toLong()
        var currentBitmap = bitmap
        var bestResult: ByteArray? = null
        var bestQuality = -1
        var iterations = 0
        val compressFormat = FormatHandler.getCompressFormat(format)
        
        var currentWidth = currentBitmap.width
        var currentHeight = currentBitmap.height
        
        while (iterations < 10) {
            yield()
            
            var low = 10
            var high = 95
            var validFound = false
            
            while (low <= high) {
                yield()
                val mid = low + (high - low) / 2
                val stream = ByteArrayOutputStream()
                currentBitmap.compress(compressFormat, mid, stream)
                val outBytes = stream.toByteArray()
                
                if (outBytes.size <= target) {
                    bestResult = outBytes
                    bestQuality = mid
                    validFound = true
                    low = mid + 1
                } else {
                    high = mid - 1
                }
            }
            
            if (validFound && bestResult != null) {
                val warning = bestQuality < 30 || currentWidth < bitmap.width * 0.25
                val res = OptimizationResult(
                    bestResult, bestQuality, currentWidth, currentHeight, warning, bestResult.size.toLong()
                )
                if (currentBitmap != bitmap) currentBitmap.recycle()
                return res
            }
            
            iterations++
            val newWidth = (currentWidth * 0.85).toInt()
            val newHeight = (currentHeight * 0.85).toInt()
            
            if (newWidth <= 0 || newHeight <= 0) break
            
            val newBitmap = Bitmap.createScaledBitmap(currentBitmap, newWidth, newHeight, true)
            if (currentBitmap != bitmap) currentBitmap.recycle()
            currentBitmap = newBitmap
            currentWidth = newWidth
            currentHeight = newHeight
        }
        
        onStepUpdate(ProcessingStep.VERIFYING)
        val stream = ByteArrayOutputStream()
        currentBitmap.compress(compressFormat, 10, stream)
        val outBytes = stream.toByteArray()
        val warning = true
        val finalRes = OptimizationResult(outBytes, 10, currentWidth, currentHeight, warning, outBytes.size.toLong())
        if (currentBitmap != bitmap) currentBitmap.recycle()
        return finalRes
    }
}
