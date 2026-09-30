package `in`.mrps.imagecompressor.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.mrps.imagecompressor.engine.CompressionConfig
import `in`.mrps.imagecompressor.engine.CompressionResult
import `in`.mrps.imagecompressor.ui.components.ImagePreview
import `in`.mrps.imagecompressor.ui.components.ResultCard
import `in`.mrps.imagecompressor.util.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    result: CompressionResult,
    originalImageUri: Uri?,
    config: CompressionConfig,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onCompressAnother: () -> Unit,
    onBack: () -> Unit
) {
    val savedBytes = result.originalSizeBytes - result.compressedSizeBytes
    val savedPercent = if (result.originalSizeBytes > 0) ((savedBytes.toFloat() / result.originalSizeBytes) * 100).toInt() else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Result") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = MaterialTheme.colorScheme.background) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onShare,
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Text("Share")
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Text("Save to Gallery")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(64.dp)
            )
            Text(
                "Compression Complete",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (savedBytes > 0) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "You saved ${FileUtils.formatFileSize(savedBytes)} ($savedPercent%)!",
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Before", style = MaterialTheme.typography.labelMedium)
                    ImagePreview(uri = originalImageUri, modifier = Modifier.height(150.dp).padding(vertical = 4.dp))
                    Text(FileUtils.formatFileSize(result.originalSizeBytes), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("After", style = MaterialTheme.typography.labelMedium)
                    ImagePreview(uri = result.outputUri, modifier = Modifier.height(150.dp).padding(vertical = 4.dp))
                    Text(FileUtils.formatFileSize(result.compressedSizeBytes), style = MaterialTheme.typography.bodyMedium, color = Color(0xFF4CAF50))
                }
            }

            ResultCard(result = result)

            TextButton(
                onClick = onCompressAnother,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Compress Another")
            }
        }
    }
}
