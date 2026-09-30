package `in`.mrps.imagecompressor.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import `in`.mrps.imagecompressor.engine.OutputFormat
import `in`.mrps.imagecompressor.engine.FormatHandler
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat

object FileUtils {
    fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) result = cursor.getString(index)
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) result = result?.substring(cut + 1)
        }
        return result ?: "image"
    }

    fun generateOutputFileName(originalName: String, format: OutputFormat): String {
        val nameWithoutExt = originalName.substringBeforeLast(".")
        val ext = FormatHandler.getFileExtension(format)
        return "${nameWithoutExt}_compressed.$ext"
    }

    fun createTempOutputFile(context: Context, format: OutputFormat): File {
        val dir = File(context.cacheDir, "compressed_images")
        if (!dir.exists()) dir.mkdirs()
        val ext = FormatHandler.getFileExtension(format)
        return File.createTempFile("COMP_", ".$ext", dir)
    }

    fun cleanTempFiles(context: Context) {
        val dir = File(context.cacheDir, "compressed_images")
        if (dir.exists()) {
            dir.listFiles()?.forEach { it.delete() }
        }
    }

    fun getShareableUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun copyToUri(context: Context, sourceFile: File, destinationUri: Uri) {
        context.contentResolver.openOutputStream(destinationUri)?.use { out ->
            sourceFile.inputStream().use { input ->
                input.copyTo(out)
            }
        }
    }

    fun createZipFile(context: Context, files: List<File>, zipName: String = "compressed_images.zip"): File {
        val zipFile = File(context.cacheDir, zipName)
        java.util.zip.ZipOutputStream(java.io.FileOutputStream(zipFile)).use { zout ->
            for (file in files) {
                java.io.FileInputStream(file).use { input ->
                    val entry = java.util.zip.ZipEntry(file.name)
                    zout.putNextEntry(entry)
                    input.copyTo(zout)
                    zout.closeEntry()
                }
            }
        }
        return zipFile
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(bytes / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    fun formatDuration(ms: Long): String {
        val sec = ms / 1000.0
        return DecimalFormat("#.#").format(sec) + " s"
    }
}
