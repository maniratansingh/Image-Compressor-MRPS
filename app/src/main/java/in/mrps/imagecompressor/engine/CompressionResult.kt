package `in`.mrps.imagecompressor.engine

import android.net.Uri

data class CompressionResult(
    val success: Boolean,
    val outputUri: Uri?,
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long,
    val originalWidth: Int,
    val originalHeight: Int,
    val outputWidth: Int,
    val outputHeight: Int,
    val outputFormat: OutputFormat,
    val processingTimeMs: Long,
    val qualityUsed: Int,
    val hasTransparency: Boolean,
    val qualityWarning: Boolean,
    val errorMessage: String?
)

data class BatchCompressionResult(
    val results: List<CompressionResult>,
    val totalProcessingTimeMs: Long,
    val successCount: Int,
    val failureCount: Int
)
