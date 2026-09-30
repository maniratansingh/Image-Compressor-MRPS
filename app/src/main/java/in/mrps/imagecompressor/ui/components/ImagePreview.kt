package `in`.mrps.imagecompressor.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun ImagePreview(
    uri: Uri?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (uri != null) {
             AsyncImage(
                 model = uri,
                 contentDescription = "Image preview",
                 contentScale = ContentScale.Fit,
                 modifier = Modifier.fillMaxSize()
             )
        } else {
            Text("No Preview Available", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
