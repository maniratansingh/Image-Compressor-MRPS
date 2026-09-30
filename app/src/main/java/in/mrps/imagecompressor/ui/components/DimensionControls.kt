package `in`.mrps.imagecompressor.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import `in`.mrps.imagecompressor.engine.DimensionMode
import `in`.mrps.imagecompressor.engine.ScaleMode

@Composable
fun DimensionControls(
    dimensionMode: DimensionMode,
    onDimensionModeChange: (DimensionMode) -> Unit,
    scaleMode: ScaleMode,
    onScaleModeChange: (ScaleMode) -> Unit,
    lockAspectRatio: Boolean,
    onLockAspectRatioChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var widthInput by remember { mutableStateOf("") }
    var heightInput by remember { mutableStateOf("") }
    
    val modes = listOf("Original", "Max", "Exact", "Width Only", "Height Only")
    var expanded by remember { mutableStateOf(false) }
    var selectedIndex by remember { 
        mutableStateOf(
            when(dimensionMode) {
                is DimensionMode.Original -> 0
                is DimensionMode.MaxDimensions -> 1
                is DimensionMode.ExactDimensions -> 2
                is DimensionMode.WidthOnly -> 3
                is DimensionMode.HeightOnly -> 4
            }
        ) 
    }

    Column(modifier = modifier) {
        Text(
            text = "Dimensions",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box {
                OutlinedButton(onClick = { expanded = true }) {
                    Text(modes[selectedIndex])
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    modes.forEachIndexed { index, modeStr ->
                        DropdownMenuItem(
                            text = { Text(modeStr) },
                            onClick = {
                                selectedIndex = index
                                expanded = false
                                val newMode = when (index) {
                                    0 -> DimensionMode.Original
                                    1 -> DimensionMode.MaxDimensions(widthInput.toIntOrNull() ?: 1000, heightInput.toIntOrNull() ?: 1000)
                                    2 -> DimensionMode.ExactDimensions(widthInput.toIntOrNull() ?: 1000, heightInput.toIntOrNull() ?: 1000)
                                    3 -> DimensionMode.WidthOnly(widthInput.toIntOrNull() ?: 1000)
                                    else -> DimensionMode.HeightOnly(heightInput.toIntOrNull() ?: 1000)
                                }
                                onDimensionModeChange(newMode)
                            }
                        )
                    }
                }
            }
        }
        
        if (dimensionMode !is DimensionMode.Original) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (dimensionMode !is DimensionMode.HeightOnly) {
                    OutlinedTextField(
                        value = widthInput,
                        onValueChange = { 
                            widthInput = it
                            updateDimensions(selectedIndex, it, heightInput, onDimensionModeChange)
                        },
                        label = { Text("Width") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                if (dimensionMode is DimensionMode.MaxDimensions || dimensionMode is DimensionMode.ExactDimensions) {
                    Text("×")
                }
                
                if (dimensionMode !is DimensionMode.WidthOnly) {
                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { 
                            heightInput = it
                            updateDimensions(selectedIndex, widthInput, it, onDimensionModeChange)
                        },
                        label = { Text("Height") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            if (dimensionMode is DimensionMode.ExactDimensions || dimensionMode is DimensionMode.MaxDimensions) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = lockAspectRatio,
                        onCheckedChange = onLockAspectRatioChange
                    )
                    Text("Lock Aspect Ratio")
                }
            }
        }
    }
}

private fun updateDimensions(
    selectedIndex: Int,
    w: String,
    h: String,
    onDimensionModeChange: (DimensionMode) -> Unit
) {
    val width = w.toIntOrNull() ?: 0
    val height = h.toIntOrNull() ?: 0
    val newMode = when (selectedIndex) {
        1 -> DimensionMode.MaxDimensions(width, height)
        2 -> DimensionMode.ExactDimensions(width, height)
        3 -> DimensionMode.WidthOnly(width)
        4 -> DimensionMode.HeightOnly(height)
        else -> DimensionMode.Original
    }
    onDimensionModeChange(newMode)
}
