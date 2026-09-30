package `in`.mrps.imagecompressor.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetSizeSelector(
    targetSizeBytes: Long?,
    onTargetSizeChange: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickSelects = listOf(100 * 1024L, 200 * 1024L, 500 * 1024L, 1024 * 1024L, 2 * 1024 * 1024L)
    val quickSelectLabels = listOf("100 KB", "200 KB", "500 KB", "1 MB", "2 MB")
    
    var customInputValue by remember { mutableStateOf("") }
    var isMb by remember { mutableStateOf(true) }
    var isCustom by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = "Target Size",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickSelects.size) { index ->
                val size = quickSelects[index]
                val label = quickSelectLabels[index]
                FilterChip(
                    selected = !isCustom && targetSizeBytes == size,
                    onClick = { 
                        isCustom = false
                        onTargetSizeChange(size)
                    },
                    label = { Text(label) },
                    modifier = Modifier.semantics {
                        contentDescription = "Select target size $label"
                    }
                )
            }
            item {
                FilterChip(
                    selected = isCustom,
                    onClick = { 
                        isCustom = true 
                        if (customInputValue.isNotEmpty()) {
                            val value = customInputValue.toDoubleOrNull() ?: 0.0
                            val bytes = if (isMb) (value * 1024 * 1024).toLong() else (value * 1024).toLong()
                            onTargetSizeChange(bytes)
                        } else {
                            onTargetSizeChange(null)
                        }
                    },
                    label = { Text("Custom") },
                    modifier = Modifier.semantics { contentDescription = "Select custom target size" }
                )
            }
        }
        
        if (isCustom) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = customInputValue,
                    onValueChange = { newValue ->
                        customInputValue = newValue
                        val value = newValue.toDoubleOrNull()
                        if (value != null) {
                            val bytes = if (isMb) (value * 1024 * 1024).toLong() else (value * 1024).toLong()
                            onTargetSizeChange(bytes)
                        } else {
                            onTargetSizeChange(null)
                        }
                    },
                    label = { Text("Size") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                
                SegmentedButtonSingleSelect(
                    isMb = isMb,
                    onToggle = { mb ->
                        isMb = mb
                        val value = customInputValue.toDoubleOrNull()
                        if (value != null) {
                            val bytes = if (isMb) (value * 1024 * 1024).toLong() else (value * 1024).toLong()
                            onTargetSizeChange(bytes)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SegmentedButtonSingleSelect(isMb: Boolean, onToggle: (Boolean) -> Unit) {
    Row {
        Button(
            onClick = { onToggle(false) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!isMb) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (!isMb) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text("KB")
        }
        Button(
            onClick = { onToggle(true) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isMb) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isMb) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text("MB")
        }
    }
}
