package `in`.mrps.imagecompressor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.mrps.imagecompressor.engine.CompressionMode
import `in`.mrps.imagecompressor.engine.OutputFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentDefaultMode: CompressionMode,
    onDefaultModeChange: (CompressionMode) -> Unit,
    currentDefaultFormat: OutputFormat,
    onDefaultFormatChange: (OutputFormat) -> Unit,
    onAboutClick: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingSection("Defaults")
            
            Text("Default Compression Mode", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.bodyLarge)
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompressionMode.values().forEach { mode ->
                    val name = if (mode == CompressionMode.QUALITY) "Quality" else "Target Size"
                    FilterChip(selected = currentDefaultMode == mode, onClick = { onDefaultModeChange(mode) }, label = { Text(name) })
                }
            }

            Text("Default Output Format", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.bodyLarge)
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutputFormat.values().forEach { format ->
                    FilterChip(selected = currentDefaultFormat == format, onClick = { onDefaultFormatChange(format) }, label = { Text(format.name) })
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingSection("About")
            ListItem(
                headlineContent = { Text("MRPS Image Compressor") },
                supportingContent = { Text("Offline, private image compression") },
                leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                modifier = Modifier.padding(horizontal = 8.dp),
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
            )
            Button(onClick = onAboutClick, modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)) {
                Text("View About")
            }
        }
    }
}

@Composable
fun SettingSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    )
}
