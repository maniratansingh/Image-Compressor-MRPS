package `in`.mrps.imagecompressor.data

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import `in`.mrps.imagecompressor.engine.FormatHandler
import `in`.mrps.imagecompressor.engine.OutputFormat
import `in`.mrps.imagecompressor.util.FileUtils

data class ImageInfo(
    val uri: Uri,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val format: OutputFormat?,
    val hasTransparency: Boolean
) {
    companion object {
        fun readImageInfo(context: Context, uri: Uri): ImageInfo {
            val name = FileUtils.getFileName(context, uri)
            var size = 0L
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { size = it.length }
            val mime = context.contentResolver.getType(uri) ?: "image/*"
            val format = FormatHandler.detectFormat(context, uri)
            
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
            
            return ImageInfo(
                uri = uri,
                fileName = name,
                mimeType = mime,
                sizeBytes = size,
                width = options.outWidth,
                height = options.outHeight,
                format = format,
                hasTransparency = format == OutputFormat.PNG || format == OutputFormat.WEBP
            )
        }
    }
}
