package `in`.mrps.imagecompressor.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.mrps.imagecompressor.engine.CompressionResult
import `in`.mrps.imagecompressor.util.FileUtils
import `in`.mrps.imagecompressor.util.FormatUtils

@Composable
fun ResultCard(
    result: CompressionResult,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Statistics", style = MaterialTheme.typography.titleMedium)
            
            StatRow("Original Size", FileUtils.formatFileSize(result.originalSizeBytes))
            StatRow("Compressed Size", FileUtils.formatFileSize(result.compressedSizeBytes))
            StatRow("Reduction", FormatUtils.formatPercentage(result.originalSizeBytes, result.compressedSizeBytes))
            StatRow("Original Dimensions", FormatUtils.formatDimensions(result.originalWidth, result.originalHeight))
            StatRow("Output Dimensions", FormatUtils.formatDimensions(result.outputWidth, result.outputHeight))
            StatRow("Format", result.outputFormat.name)
            StatRow("Processing Time", FileUtils.formatDuration(result.processingTimeMs))
            
            if (result.qualityWarning) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Warning: Quality dropped significantly to meet target size.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
