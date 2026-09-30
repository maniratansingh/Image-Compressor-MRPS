package `in`.mrps.imagecompressor.engine

enum class CompressionMode { QUALITY, TARGET_SIZE }

enum class OutputFormat { JPEG, PNG, WEBP }

sealed class DimensionMode {
    object Original : DimensionMode()
    data class MaxDimensions(val maxWidth: Int, val maxHeight: Int) : DimensionMode()
    data class ExactDimensions(val width: Int, val height: Int) : DimensionMode()
    data class WidthOnly(val width: Int) : DimensionMode()
    data class HeightOnly(val height: Int) : DimensionMode()
}

enum class ScaleMode { FIT, CROP }

data class CompressionConfig(
    val mode: CompressionMode = CompressionMode.QUALITY,
    val quality: Int = 85,
    val targetSizeBytes: Long? = null,
    val outputFormat: OutputFormat = OutputFormat.JPEG,
    val dimensionMode: DimensionMode = DimensionMode.Original,
    val scaleMode: ScaleMode = ScaleMode.FIT,
    val lockAspectRatio: Boolean = true
)

enum class ProcessingStep { READING, METADATA, ORIENTATION, PREPARING, DIMENSIONS, QUALITY, ENCODING, VERIFYING }

data class ProcessingProgress(
    val currentStep: ProcessingStep,
    val completedSteps: Int,
    val totalSteps: Int,
    val currentFileName: String,
    val batchIndex: Int,
    val batchTotal: Int,
    val elapsedMs: Long,
    val estimatedRemainingMs: Long
)
