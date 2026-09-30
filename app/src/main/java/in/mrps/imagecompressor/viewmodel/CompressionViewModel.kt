package `in`.mrps.imagecompressor.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.mrps.imagecompressor.data.ImageInfo
import `in`.mrps.imagecompressor.engine.BatchCompressionResult
import `in`.mrps.imagecompressor.engine.CompressionConfig
import `in`.mrps.imagecompressor.engine.CompressionMode
import `in`.mrps.imagecompressor.engine.CompressionResult
import `in`.mrps.imagecompressor.engine.DimensionMode
import `in`.mrps.imagecompressor.engine.ImageCompressor
import `in`.mrps.imagecompressor.engine.OutputFormat
import `in`.mrps.imagecompressor.engine.ProcessingProgress
import `in`.mrps.imagecompressor.engine.ScaleMode
import `in`.mrps.imagecompressor.engine.FormatHandler
import `in`.mrps.imagecompressor.util.FileUtils
import `in`.mrps.imagecompressor.util.GalleryUtils
import `in`.mrps.imagecompressor.util.ShareUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CompressionViewModel : ViewModel() {

    private val compressor = ImageCompressor()

    private val _selectedImages = MutableStateFlow<List<ImageInfo>>(emptyList())
    val selectedImages: StateFlow<List<ImageInfo>> = _selectedImages.asStateFlow()

    private val _compressionConfig = MutableStateFlow(
        CompressionConfig(
            mode = CompressionMode.QUALITY,
            quality = 85,
            targetSizeBytes = null,
            outputFormat = OutputFormat.JPEG,
            dimensionMode = DimensionMode.Original,
            scaleMode = ScaleMode.FIT,
            lockAspectRatio = true
        )
    )
    val compressionConfig: StateFlow<CompressionConfig> = _compressionConfig.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _processingProgress = MutableStateFlow<ProcessingProgress?>(null)
    val processingProgress: StateFlow<ProcessingProgress?> = _processingProgress.asStateFlow()

    private val _result = MutableStateFlow<CompressionResult?>(null)
    val result: StateFlow<CompressionResult?> = _result.asStateFlow()

    private val _batchResult = MutableStateFlow<BatchCompressionResult?>(null)
    val batchResult: StateFlow<BatchCompressionResult?> = _batchResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _outputFiles = MutableStateFlow<List<File>>(emptyList())

    private var compressionJob: Job? = null

    private val _pendingSaveFile = MutableStateFlow<File?>(null)
    val pendingSaveFile: StateFlow<File?> = _pendingSaveFile.asStateFlow()

    private val _pendingSaveIntent = MutableStateFlow<Intent?>(null)
    val pendingSaveIntent: StateFlow<Intent?> = _pendingSaveIntent.asStateFlow()

    fun selectImages(uris: List<Uri>, context: Context) {
        viewModelScope.launch {
            val infos = withContext(Dispatchers.IO) {
                uris.mapNotNull { uri ->
                    try {
                        ImageInfo.readImageInfo(context, uri)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            _selectedImages.value = infos
        }
    }

    fun setConfig(config: CompressionConfig) {
        _compressionConfig.value = config
    }

    fun startCompression(context: Context) {
        val images = _selectedImages.value
        if (images.isEmpty()) return

        _isProcessing.value = true
        _error.value = null
        _result.value = null
        _batchResult.value = null
        _outputFiles.value = emptyList()

        compressionJob = viewModelScope.launch {
            try {
                val config = _compressionConfig.value
                val uris = images.map { it.uri }

                if (uris.size == 1) {
                    val compressionResult = compressor.compress(
                        context = context,
                        sourceUri = uris.first(),
                        config = config,
                        onProgress = { progress ->
                            _processingProgress.value = progress
                        }
                    )
                    _result.value = compressionResult

                    if (compressionResult.success && compressionResult.outputUri != null) {
                        val outputFile = getFileFromUri(context, compressionResult.outputUri)
                        if (outputFile != null) {
                            _outputFiles.value = listOf(outputFile)
                        }
                    }
                } else {
                    val batchCompressionResult = compressor.compressBatch(
                        context = context,
                        sourceUris = uris,
                        config = config,
                        onProgress = { progress ->
                            _processingProgress.value = progress
                        }
                    )
                    _batchResult.value = batchCompressionResult

                    val files = batchCompressionResult.results
                        .filter { it.success && it.outputUri != null }
                        .mapNotNull { getFileFromUri(context, it.outputUri) }
                    _outputFiles.value = files
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun updateSingleImageUri(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val info = withContext(Dispatchers.IO) { ImageInfo.readImageInfo(context, uri) }
                _selectedImages.value = listOf(info)
            } catch (e: Exception) { }
        }
    }

    fun cancelCompression() {
        compressionJob?.cancel()
        _isProcessing.value = false
        _processingProgress.value = null
    }

    fun saveResult(context: Context, index: Int = 0) {
        viewModelScope.launch(Dispatchers.IO) {
            val files = _outputFiles.value
            if (index < 0 || index >= files.size) return@launch

            val file = files[index]
            val res = if (index == 0 && _result.value != null) _result.value else {
                _batchResult.value?.results?.getOrNull(index)
            }
            val format = res?.outputFormat ?: OutputFormat.JPEG
            val mimeType = FormatHandler.getMimeType(format)

            val success = GalleryUtils.saveImageToGallery(context, file, mimeType)
            
            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(context, "Saved to Gallery", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun completeSave(context: Context, destinationUri: Uri) {
        val file = _pendingSaveFile.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                FileUtils.copyToUri(context, file, destinationUri)
            } catch (e: Exception) {
                _error.value = "Failed to save: ${e.message}"
            }
        }
        _pendingSaveFile.value = null
        _pendingSaveIntent.value = null
    }

    fun shareResult(context: Context, index: Int = 0) {
        val files = _outputFiles.value
        if (index < 0 || index >= files.size) return

        val file = files[index]
        val res = if (index == 0 && _result.value != null) _result.value else {
            _batchResult.value?.results?.getOrNull(index)
        }
        val format = res?.outputFormat ?: OutputFormat.JPEG
        val mimeType = FormatHandler.getMimeType(format)

        ShareUtils.shareImage(context, file, mimeType)
    }

    fun saveAll(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val files = _outputFiles.value
            if (files.isEmpty()) return@launch

            var savedCount = 0
            for (i in files.indices) {
                val file = files[i]
                val res = _batchResult.value?.results?.getOrNull(i)
                val format = res?.outputFormat ?: OutputFormat.JPEG
                val mimeType = FormatHandler.getMimeType(format)
                if (GalleryUtils.saveImageToGallery(context, file, mimeType)) {
                    savedCount++
                }
            }
            
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Saved $savedCount images to Gallery", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun shareAll(context: Context) {
        val files = _outputFiles.value
        if (files.isEmpty()) return

        val format = _compressionConfig.value.outputFormat
        val mimeType = FormatHandler.getMimeType(format)
        ShareUtils.shareImages(context, files, mimeType)
    }

    fun reset() {
        _selectedImages.value = emptyList()
        _result.value = null
        _batchResult.value = null
        _processingProgress.value = null
        _error.value = null
        _isProcessing.value = false
        _outputFiles.value = emptyList()
        _pendingSaveFile.value = null
        _pendingSaveIntent.value = null
    }

    private fun getFileFromUri(context: Context, uri: Uri?): File? {
        if (uri == null) return null
        val fileName = uri.lastPathSegment ?: return null
        val cacheDir = File(context.cacheDir, "compressed_images")
        val file = File(cacheDir, fileName)
        return if (file.exists()) file else null
    }

    override fun onCleared() {
        super.onCleared()
        compressionJob?.cancel()
    }
}
