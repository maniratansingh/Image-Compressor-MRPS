package `in`.mrps.imagecompressor.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.mrps.imagecompressor.engine.OutputFormat

@Composable
fun FormatSelector(
    selectedFormat: OutputFormat,
    onFormatSelected: (OutputFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Output Format",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutputFormat.values().forEach { format ->
                val label = when (format) {
                    OutputFormat.JPEG -> "JPG / JPEG"
                    OutputFormat.PNG -> "PNG (Lossless)"
                    OutputFormat.WEBP -> "WEBP"
                }
                
                FilterChip(
                    selected = selectedFormat == format,
                    onClick = { onFormatSelected(format) },
                    label = { Text(label) }
                )
            }
        }
    }
}
