package `in`.mrps.imagecompressor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.mrps.imagecompressor.engine.ProcessingProgress
import `in`.mrps.imagecompressor.ui.components.ProgressTimeline
import `in`.mrps.imagecompressor.util.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessingScreen(
    progress: ProcessingProgress?,
    onCancel: () -> Unit
) {
    
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Processing...",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (progress != null) {
                if (progress.batchTotal > 1) {
                    Text(
                        text = "Compressing ${progress.batchIndex + 1} / ${progress.batchTotal}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Text(
                    text = progress.currentFileName,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                LinearProgressIndicator(
                    progress = { if (progress.totalSteps > 0) progress.completedSteps.toFloat() / progress.totalSteps else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .padding(bottom = 32.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )

                ProgressTimeline(
                    currentStep = progress.currentStep,
                    completedSteps = progress.completedSteps,
                    totalSteps = progress.totalSteps,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Elapsed: ${FileUtils.formatDuration(progress.elapsedMs)}", style = MaterialTheme.typography.bodySmall)
                    if (progress.estimatedRemainingMs != null) {
                        Text("Remaining: ${FileUtils.formatDuration(progress.estimatedRemainingMs!!)}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                CircularProgressIndicator(modifier = Modifier.padding(bottom = 32.dp))
            }

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}
