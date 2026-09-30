package `in`.mrps.imagecompressor.util

import java.text.DecimalFormat

object FormatUtils {
    fun formatPercentage(original: Long, compressed: Long): String {
        if (original <= 0) return "0%"
        val pct = (compressed.toDouble() / original.toDouble()) * 100
        return DecimalFormat("#.#").format(pct) + "%"
    }

    fun formatDimensions(width: Int, height: Int): String {
        return "$width × $height px"
    }

    fun formatTargetSize(bytes: Long): String {
        return FileUtils.formatFileSize(bytes)
    }

    fun parseTargetSize(value: String, unit: String): Long? {
        val d = value.toDoubleOrNull() ?: return null
        return when (unit.uppercase()) {
            "KB" -> (d * 1024).toLong()
            "MB" -> (d * 1024 * 1024).toLong()
            else -> d.toLong()
        }
    }
}
