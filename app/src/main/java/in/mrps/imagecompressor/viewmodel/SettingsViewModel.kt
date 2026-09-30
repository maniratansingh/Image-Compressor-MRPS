package `in`.mrps.imagecompressor.viewmodel

import androidx.lifecycle.ViewModel
import `in`.mrps.imagecompressor.engine.CompressionMode
import `in`.mrps.imagecompressor.engine.OutputFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel : ViewModel() {
    private val _defaultMode = MutableStateFlow(CompressionMode.QUALITY)
    val defaultMode: StateFlow<CompressionMode> = _defaultMode.asStateFlow()

    private val _defaultFormat = MutableStateFlow(OutputFormat.JPEG)
    val defaultFormat: StateFlow<OutputFormat> = _defaultFormat.asStateFlow()

    fun setDefaultMode(mode: CompressionMode) {
        _defaultMode.value = mode
    }

    fun setDefaultFormat(format: OutputFormat) {
        _defaultFormat.value = format
    }
}
