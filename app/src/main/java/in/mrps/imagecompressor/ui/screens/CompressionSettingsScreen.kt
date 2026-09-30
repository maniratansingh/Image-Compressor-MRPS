
package `in`.mrps.imagecompressor.ui.screens
import androidx.compose.material.icons.automirrored.filled.ArrowForward


import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.mrps.imagecompressor.data.ImageInfo
import `in`.mrps.imagecompressor.engine.*
import `in`.mrps.imagecompressor.ui.components.*
import `in`.mrps.imagecompressor.util.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompressionSettingsScreen(
    selectedImages: List<ImageInfo>,
    onCompress: (CompressionConfig) -> Unit,
    onCrop: (Uri) -> Unit,
    onBack: () -> Unit
) {
    var mode by remember { mutableStateOf(CompressionMode.QUALITY) }
    var quality by remember { mutableStateOf(85f) }
    var targetSizeBytes by remember { mutableStateOf<Long?>(null) }
    var dimensionMode by remember { mutableStateOf<DimensionMode>(DimensionMode.Original) }
    var scaleMode by remember { mutableStateOf(ScaleMode.FIT) }
    var lockAspectRatio by remember { mutableStateOf(true) }
    var format by remember { mutableStateOf(OutputFormat.JPEG) }

    val totalOriginalBytes = selectedImages.sumOf { it.sizeBytes }
    
    val estimatedBytes = remember(mode, quality, targetSizeBytes, totalOriginalBytes) {
        if (mode == CompressionMode.TARGET_SIZE) {
            (targetSizeBytes ?: 0L) * selectedImages.size
        } else {
            val ratio = (quality / 100f) * 0.8f
            (totalOriginalBytes * ratio).toLong().coerceAtMost(totalOriginalBytes)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compression Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = MaterialTheme.colorScheme.background) {
                Button(
                    onClick = {
                        onCompress(
                            CompressionConfig(
                                mode = mode,
                                quality = quality.toInt(),
                                targetSizeBytes = targetSizeBytes,
                                outputFormat = format,
                                dimensionMode = dimensionMode,
                                scaleMode = scaleMode,
                                lockAspectRatio = lockAspectRatio
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp)
                ) {
                    Text("Compress")
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (selectedImages.size == 1) {
                val uri = selectedImages.first().uri
                Box {
                    ImagePreview(uri = uri)
                    FilledTonalIconButton(
                        onClick = { onCrop(uri) },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Crop Image")
                    }
                }
            } else {
                Text("${selectedImages.size} images selected", style = MaterialTheme.typography.titleMedium)
            }

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Current Size", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = FileUtils.formatFileSize(totalOriginalBytes), 
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.padding(horizontal = 8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Estimated After", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = if (estimatedBytes > 0) "~${FileUtils.formatFileSize(estimatedBytes)}" else "---", 
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            Column {
                Text("Mode", style = MaterialTheme.typography.titleMedium)
                Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = mode == CompressionMode.QUALITY,
                        onClick = { mode = CompressionMode.QUALITY },
                        label = { Text("Quality Slider") }
                    )
                    FilterChip(
                        selected = mode == CompressionMode.TARGET_SIZE,
                        onClick = { mode = CompressionMode.TARGET_SIZE },
                        label = { Text("Exact File Size") }
                    )
                }
            }

            if (mode == CompressionMode.QUALITY) {
                Column {
                    Text("Quality: ${quality.toInt()}%", style = MaterialTheme.typography.titleMedium)
                    Slider(
                        value = quality,
                        onValueChange = { quality = it },
                        valueRange = 1f..100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Lower quality = smaller file size. 85% is a good default.", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                TargetSizeSelector(
                    targetSizeBytes = targetSizeBytes,
                    onTargetSizeChange = { targetSizeBytes = it }
                )
            }

            HorizontalDivider()

            DimensionControls(
                dimensionMode = dimensionMode,
                onDimensionModeChange = { dimensionMode = it },
                scaleMode = scaleMode,
                onScaleModeChange = { scaleMode = it },
                lockAspectRatio = lockAspectRatio,
                onLockAspectRatioChange = { lockAspectRatio = it }
            )

            HorizontalDivider()

            FormatSelector(
                selectedFormat = format,
                onFormatSelected = { format = it }
            )
        }
    }
}
