package `in`.mrps.imagecompressor.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import `in`.mrps.imagecompressor.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ImageCompressor {
    private val optimizer = TargetSizeOptimizer()
    private val estimator = PerformanceEstimator()

    suspend fun compressBatch(
        context: Context,
        sourceUris: List<Uri>,
        config: CompressionConfig,
        onProgress: (ProcessingProgress) -> Unit
    ): BatchCompressionResult = withContext(Dispatchers.Default) {
        val results = mutableListOf<CompressionResult>()
        val startBatchTime = System.currentTimeMillis()
        var successes = 0
        var failures = 0
        
        for ((index, uri) in sourceUris.withIndex()) {
            ensureActive()
            val res = compressSingle(context, uri, config, index, sourceUris.size, onProgress)
            results.add(res)
            if (res.success) successes++ else failures++
        }
        
        BatchCompressionResult(
            results = results,
            totalProcessingTimeMs = System.currentTimeMillis() - startBatchTime,
            successCount = successes,
            failureCount = failures
        )
    }

    suspend fun compress(
        context: Context,
        sourceUri: Uri,
        config: CompressionConfig,
        onProgress: (ProcessingProgress) -> Unit
    ): CompressionResult = compressSingle(context, sourceUri, config, 0, 1, onProgress)

    private suspend fun compressSingle(
        context: Context,
        sourceUri: Uri,
        config: CompressionConfig,
        batchIndex: Int,
        batchTotal: Int,
        onProgress: (ProcessingProgress) -> Unit
    ): CompressionResult = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()
        var currentStep = ProcessingStep.READING
        
        val fileName = FileUtils.getFileName(context, sourceUri)
        
        fun sendProgress(step: ProcessingStep) {
            currentStep = step
            val stepsList = ProcessingStep.values()
            val completed = stepsList.indexOf(step)
            val elapsed = System.currentTimeMillis() - startTime
            onProgress(
                ProcessingProgress(
                    currentStep = step,
                    completedSteps = completed,
                    totalSteps = stepsList.size,
                    currentFileName = fileName,
                    batchIndex = batchIndex,
                    batchTotal = batchTotal,
                    elapsedMs = elapsed,
                    estimatedRemainingMs = 0
                )
            )
        }
        
        try {
            sendProgress(ProcessingStep.READING)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(sourceUri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
            
            val originalWidth = options.outWidth
            val originalHeight = options.outHeight
            var origSize = 0L
            context.contentResolver.openAssetFileDescriptor(sourceUri, "r")?.use {
                origSize = it.length
            }
            
            sendProgress(ProcessingStep.ORIENTATION)
            val orientation = ExifHandler.readOrientation(context, sourceUri)
            val isRotated = orientation == androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 || 
                            orientation == androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270
            
            val effOrigWidth = if (isRotated) originalHeight else originalWidth
            val effOrigHeight = if (isRotated) originalWidth else originalHeight
            
            sendProgress(ProcessingStep.DIMENSIONS)
            val dimenMode = config.dimensionMode
                            
            val (targetWidth, targetHeight) = DimensionCalculator.calculateDimensions(
                effOrigWidth, effOrigHeight, dimenMode, config.scaleMode, config.lockAspectRatio
            )
            
            sendProgress(ProcessingStep.PREPARING)
            val sampleSize = DimensionCalculator.calculateInSampleSize(effOrigWidth, effOrigHeight, targetWidth, targetHeight)
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            var bitmap = context.contentResolver.openInputStream(sourceUri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            } ?: throw Exception("Failed to decode image")
            
            bitmap = ExifHandler.applyOrientation(bitmap, orientation)
            val hasTransp = FormatHandler.hasTransparency(bitmap)
            
            if (bitmap.width != targetWidth || bitmap.height != targetHeight) {
                val scaled = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
                if (scaled != bitmap) bitmap.recycle()
                bitmap = scaled
            }
            
            sendProgress(ProcessingStep.ENCODING)
            
            var outBytes: ByteArray
            var finalQuality = config.quality
            var finalWidth = bitmap.width
            var finalHeight = bitmap.height
            var warning = false
            
            if (config.mode == CompressionMode.TARGET_SIZE && config.targetSizeBytes != null) {
                val result = optimizer.optimize(bitmap, config.targetSizeBytes, config.outputFormat) { step ->
                    sendProgress(step)
                }
                outBytes = result.outputBytes
                finalQuality = result.quality
                finalWidth = result.width
                finalHeight = result.height
                warning = result.qualityWarning
            } else {
                val stream = ByteArrayOutputStream()
                bitmap.compress(FormatHandler.getCompressFormat(config.outputFormat), config.quality, stream)
                outBytes = stream.toByteArray()
            }
            
            bitmap.recycle()
            
            sendProgress(ProcessingStep.VERIFYING)
            
            val outFile = FileUtils.createTempOutputFile(context, config.outputFormat)
            FileOutputStream(outFile).use { it.write(outBytes) }
            val outUri = FileUtils.getShareableUri(context, outFile)
            
            CompressionResult(
                success = true,
                outputUri = outUri,
                originalSizeBytes = origSize,
                compressedSizeBytes = outBytes.size.toLong(),
                originalWidth = effOrigWidth,
                originalHeight = effOrigHeight,
                outputWidth = finalWidth,
                outputHeight = finalHeight,
                outputFormat = config.outputFormat,
                processingTimeMs = System.currentTimeMillis() - startTime,
                qualityUsed = finalQuality,
                hasTransparency = hasTransp,
                qualityWarning = warning,
                errorMessage = null
            )
            
        } catch (e: Exception) {
            CompressionResult(
                success = false,
                outputUri = null,
                originalSizeBytes = 0,
                compressedSizeBytes = 0,
                originalWidth = 0,
                originalHeight = 0,
                outputWidth = 0,
                outputHeight = 0,
                outputFormat = config.outputFormat,
                processingTimeMs = System.currentTimeMillis() - startTime,
                qualityUsed = 0,
                hasTransparency = false,
                qualityWarning = false,
                errorMessage = e.message ?: "Unknown error"
            )
        }
    }
}
