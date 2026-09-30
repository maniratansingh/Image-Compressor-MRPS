package `in`.mrps.imagecompressor.engine

import kotlin.math.max

object DimensionCalculator {
    fun calculateDimensions(
        sourceWidth: Int,
        sourceHeight: Int,
        dimensionMode: DimensionMode,
        scaleMode: ScaleMode,
        lockAspectRatio: Boolean
    ): Pair<Int, Int> {
        if (sourceWidth <= 0 || sourceHeight <= 0) return Pair(1, 1)

        val ratio = sourceWidth.toFloat() / sourceHeight.toFloat()

        var outWidth = sourceWidth
        var outHeight = sourceHeight

        when (dimensionMode) {
            is DimensionMode.Original -> {
                outWidth = sourceWidth
                outHeight = sourceHeight
            }
            is DimensionMode.MaxDimensions -> {
                if (sourceWidth > dimensionMode.maxWidth || sourceHeight > dimensionMode.maxHeight) {
                    val widthRatio = dimensionMode.maxWidth.toFloat() / sourceWidth
                    val heightRatio = dimensionMode.maxHeight.toFloat() / sourceHeight
                    val scale = if (widthRatio < heightRatio) widthRatio else heightRatio
                    outWidth = (sourceWidth * scale).toInt()
                    outHeight = (sourceHeight * scale).toInt()
                }
            }
            is DimensionMode.ExactDimensions -> {
                if (lockAspectRatio) {
                    val widthRatio = dimensionMode.width.toFloat() / sourceWidth
                    val heightRatio = dimensionMode.height.toFloat() / sourceHeight
                    val scale = if (scaleMode == ScaleMode.FIT) {
                        if (widthRatio < heightRatio) widthRatio else heightRatio
                    } else {
                        if (widthRatio > heightRatio) widthRatio else heightRatio
                    }
                    outWidth = (sourceWidth * scale).toInt()
                    outHeight = (sourceHeight * scale).toInt()
                } else {
                    outWidth = dimensionMode.width
                    outHeight = dimensionMode.height
                }
            }
            is DimensionMode.WidthOnly -> {
                outWidth = dimensionMode.width
                outHeight = if (lockAspectRatio) (dimensionMode.width / ratio).toInt() else sourceHeight
            }
            is DimensionMode.HeightOnly -> {
                outHeight = dimensionMode.height
                outWidth = if (lockAspectRatio) (dimensionMode.height * ratio).toInt() else sourceWidth
            }
        }

        return Pair(max(1, outWidth), max(1, outHeight))
    }

    fun calculateInSampleSize(sourceWidth: Int, sourceHeight: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (sourceHeight > reqHeight || sourceWidth > reqWidth) {
            val halfHeight = sourceHeight / 2
            val halfWidth = sourceWidth / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun needsAspectRatioChange(sourceWidth: Int, sourceHeight: Int, targetWidth: Int, targetHeight: Int): Boolean {
        if (sourceHeight == 0 || targetHeight == 0) return false
        val sourceRatio = sourceWidth.toFloat() / sourceHeight
        val targetRatio = targetWidth.toFloat() / targetHeight
        return Math.abs(sourceRatio - targetRatio) > 0.01f
    }
}
