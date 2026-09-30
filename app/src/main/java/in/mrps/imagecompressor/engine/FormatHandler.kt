package `in`.mrps.imagecompressor.engine

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.MimeTypeMap

object FormatHandler {
    fun detectFormat(context: Context, uri: Uri): OutputFormat? {
        val mimeType = context.contentResolver.getType(uri)
        return when (mimeType) {
            "image/png" -> OutputFormat.PNG
            "image/webp" -> OutputFormat.WEBP
            "image/jpeg", "image/jpg" -> OutputFormat.JPEG
            else -> {
                val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())?.lowercase()
                when (extension) {
                    "png" -> OutputFormat.PNG
                    "webp" -> OutputFormat.WEBP
                    "jpg", "jpeg" -> OutputFormat.JPEG
                    else -> null
                }
            }
        }
    }

    fun hasTransparency(bitmap: Bitmap): Boolean {
        return bitmap.hasAlpha()
    }

    fun getCompressFormat(format: OutputFormat): Bitmap.CompressFormat {
        return when (format) {
            OutputFormat.JPEG -> Bitmap.CompressFormat.JPEG
            OutputFormat.PNG -> Bitmap.CompressFormat.PNG
            OutputFormat.WEBP -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
            }
        }
    }

    fun getMimeType(format: OutputFormat): String {
        return when (format) {
            OutputFormat.JPEG -> "image/jpeg"
            OutputFormat.PNG -> "image/png"
            OutputFormat.WEBP -> "image/webp"
        }
    }

    fun getFileExtension(format: OutputFormat): String {
        return when (format) {
            OutputFormat.JPEG -> "jpg"
            OutputFormat.PNG -> "png"
            OutputFormat.WEBP -> "webp"
        }
    }

    fun supportsTransparency(format: OutputFormat): Boolean {
        return format == OutputFormat.PNG || format == OutputFormat.WEBP
    }
}
