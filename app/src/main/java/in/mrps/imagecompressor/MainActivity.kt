package `in`.mrps.imagecompressor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import `in`.mrps.imagecompressor.ui.theme.ImageCompressorTheme
import `in`.mrps.imagecompressor.viewmodel.CompressionViewModel
import `in`.mrps.imagecompressor.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    private val compressionViewModel: CompressionViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        handleIntent(intent)

        setContent {
            ImageCompressorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ImageCompressorApp(
                        compressionViewModel = compressionViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        
        if (intent.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
            (intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))?.let { uri ->
                compressionViewModel.selectImages(listOf(uri), this)
            }
        } else if (intent.action == Intent.ACTION_SEND_MULTIPLE && intent.type?.startsWith("image/") == true) {
            intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.let { uris ->
                if (uris.isNotEmpty()) {
                    compressionViewModel.selectImages(uris.toList(), this)
                }
            }
        }
    }
}
