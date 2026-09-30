package `in`.mrps.imagecompressor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.mrps.imagecompressor.data.ImageInfo
import `in`.mrps.imagecompressor.engine.BatchCompressionResult
import `in`.mrps.imagecompressor.ui.components.ImagePreview
import `in`.mrps.imagecompressor.util.FileUtils
import `in`.mrps.imagecompressor.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchResultScreen(
    batchResult: BatchCompressionResult,
    imageInfos: List<ImageInfo>,
    onSaveAll: () -> Unit,
    onShareAll: () -> Unit,
    onSaveItem: (Int) -> Unit,
    onShareItem: (Int) -> Unit,
    onCompressAnother: () -> Unit,
    onBack: () -> Unit
) {
    val totalOriginal = batchResult.results.sumOf { it.originalSizeBytes }
    val totalCompressed = batchResult.results.sumOf { it.compressedSizeBytes }
    val totalSaved = totalOriginal - totalCompressed
    val savedPercent = if (totalOriginal > 0) ((totalSaved.toFloat() / totalOriginal) * 100).toInt() else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Batch Results") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = MaterialTheme.colorScheme.background) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(onClick = onShareAll, modifier = Modifier.weight(1f).height(56.dp)) {
                        Text("Share All")
                    }
                    Button(onClick = onSaveAll, modifier = Modifier.weight(1f).height(56.dp)) {
                        Text("Save All to Gallery")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${batchResult.successCount} images compressed successfully", style = MaterialTheme.typography.titleMedium)
                    
                    if (totalSaved > 0) {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                        ) {
                            Text(
                                text = "Total Storage Saved: ${FileUtils.formatFileSize(totalSaved)} ($savedPercent%)",
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (batchResult.failureCount > 0) {
                        Text("${batchResult.failureCount} failed", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                    }
                    Text("Total Time: ${FileUtils.formatDuration(batchResult.totalProcessingTimeMs)}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = onCompressAnother, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Compress Another Batch")
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(batchResult.results) { index, res ->
                    val info = imageInfos.getOrNull(index)
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(info?.fileName ?: "Image ${index + 1}", style = MaterialTheme.typography.titleSmall)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f).height(100.dp)) {
                                    ImagePreview(uri = info?.uri)
                                }
                                Box(modifier = Modifier.weight(1f).height(100.dp)) {
                                    ImagePreview(uri = res.outputUri)
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("Before: ${FileUtils.formatFileSize(res.originalSizeBytes)}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.error)
                                Text("After: ${FileUtils.formatFileSize(res.compressedSizeBytes)}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), color = Color(0xFF4CAF50))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { onShareItem(index) }) { Text("Share") }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = { onSaveItem(index) }) { Text("Save to Gallery") }
                            }
                        }
                    }
                }
            }
        }
    }
}
