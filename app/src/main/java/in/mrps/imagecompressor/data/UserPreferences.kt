package `in`.mrps.imagecompressor.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import `in`.mrps.imagecompressor.engine.CompressionMode
import `in`.mrps.imagecompressor.engine.OutputFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {
    companion object {
        val DEFAULT_COMPRESSION_MODE = stringPreferencesKey("default_compression_mode")
        val OUTPUT_FORMAT = stringPreferencesKey("output_format")
    }

    val defaultCompressionMode: Flow<CompressionMode> = context.dataStore.data.map {
        try {
            CompressionMode.valueOf(it[DEFAULT_COMPRESSION_MODE] ?: CompressionMode.QUALITY.name)
        } catch (e: Exception) {
            CompressionMode.QUALITY
        }
    }

    val outputFormat: Flow<OutputFormat> = context.dataStore.data.map {
        OutputFormat.valueOf(it[OUTPUT_FORMAT] ?: OutputFormat.JPEG.name)
    }
}
