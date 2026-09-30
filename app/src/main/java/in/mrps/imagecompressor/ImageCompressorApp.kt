package `in`.mrps.imagecompressor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import `in`.mrps.imagecompressor.engine.CompressionConfig
import `in`.mrps.imagecompressor.ui.screens.*
import `in`.mrps.imagecompressor.viewmodel.CompressionViewModel
import `in`.mrps.imagecompressor.viewmodel.SettingsViewModel

@Composable
fun ImageCompressorApp(
    compressionViewModel: CompressionViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    val selectedImages by compressionViewModel.selectedImages.collectAsState()
    val config by compressionViewModel.compressionConfig.collectAsState()
    val progress by compressionViewModel.processingProgress.collectAsState()
    val result by compressionViewModel.result.collectAsState()
    val batchResult by compressionViewModel.batchResult.collectAsState()
    val error by compressionViewModel.error.collectAsState()
    val pendingSaveIntent by compressionViewModel.pendingSaveIntent.collectAsState()
    
    val defaultMode by settingsViewModel.defaultMode.collectAsState()
    val defaultFormat by settingsViewModel.defaultFormat.collectAsState()

    var pendingNavigation by remember { mutableStateOf<String?>(null) }

    val singlePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            compressionViewModel.selectImages(listOf(uri), context)
            pendingNavigation?.let { route ->
                navController.navigate(route)
                pendingNavigation = null
            }
        }
    }

    val multiPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            compressionViewModel.selectImages(uris, context)
            pendingNavigation?.let { route ->
                navController.navigate(route)
                pendingNavigation = null
            }
        }
    }

    val cropImageLauncher = rememberLauncherForActivityResult(CropImageContract()) { res ->
        if (res.isSuccessful) {
            val uriContent = res.uriContent
            if (uriContent != null) {
                compressionViewModel.updateSingleImageUri(uriContent, context)
            }
        }
    }

    val saveFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if (activityResult.resultCode == android.app.Activity.RESULT_OK) {
            activityResult.data?.data?.let { uri ->
                compressionViewModel.completeSave(context, uri)
            }
        }
    }

    LaunchedEffect(pendingSaveIntent) {
        pendingSaveIntent?.let { intent ->
            saveFileLauncher.launch(intent)
        }
    }

    LaunchedEffect(selectedImages) {
        if (selectedImages.isNotEmpty() && navController.currentDestination?.route == "home") {
            navController.navigate("compressionSettings")
        }
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onSingleCompress = {
                    pendingNavigation = "compressionSettings"
                    singlePickerLauncher.launch("image/*")
                },
                onBatchCompress = {
                    pendingNavigation = "compressionSettings"
                    multiPickerLauncher.launch("image/*")
                },
                onSettings = { navController.navigate("settings") }
            )
        }
        
        composable("compressionSettings") {
            if (selectedImages.isEmpty()) {
                LaunchedEffect(Unit) { navController.popBackStack() }
                return@composable
            }
            CompressionSettingsScreen(
                selectedImages = selectedImages,
                onCompress = { finalConfig ->
                    compressionViewModel.setConfig(finalConfig)
                    compressionViewModel.startCompression(context)
                    navController.navigate("processing")
                },
                onCrop = { uri ->
                    cropImageLauncher.launch(
                        CropImageContractOptions(uri, CropImageOptions())
                    )
                },
                onBack = { 
                    compressionViewModel.reset()
                    navController.popBackStack() 
                }
            )
        }
        
        composable("processing") {
            ProcessingScreen(
                progress = progress,
                onCancel = {
                    compressionViewModel.cancelCompression()
                    navController.popBackStack()
                }
            )
            
            
            LaunchedEffect(error) {
                if (error != null) {
                    android.widget.Toast.makeText(context, "Error: $error", android.widget.Toast.LENGTH_LONG).show()
                    compressionViewModel.reset()
                    navController.popBackStack()
                }
            }
            LaunchedEffect(result, batchResult) {
                if (result != null) {
                    navController.navigate("result") { popUpTo("home") { inclusive = false } }
                } else if (batchResult != null) {
                    navController.navigate("batchResult") { popUpTo("home") { inclusive = false } }
                }
            }
        }
        
        composable("result") {
            result?.let { res ->
                ResultScreen(
                    result = res,
                    originalImageUri = selectedImages.firstOrNull()?.uri,
                    config = config,
                    onSave = { compressionViewModel.saveResult(context, 0) },
                    onShare = { compressionViewModel.shareResult(context, 0) },
                    onCompressAnother = {
                        compressionViewModel.reset()
                        navController.popBackStack("home", inclusive = false)
                    },
                    onBack = {
                        compressionViewModel.reset()
                        navController.popBackStack("home", inclusive = false)
                    }
                )
            } ?: LaunchedEffect(Unit) { navController.popBackStack() }
        }
        
        composable("batchResult") {
            batchResult?.let { res ->
                BatchResultScreen(
                    batchResult = res,
                    imageInfos = selectedImages,
                    onSaveAll = { compressionViewModel.saveAll(context) },
                    onShareAll = { compressionViewModel.shareAll(context) },
                    onSaveItem = { compressionViewModel.saveResult(context, it) },
                    onShareItem = { compressionViewModel.shareResult(context, it) },
                    onCompressAnother = {
                        compressionViewModel.reset()
                        navController.popBackStack("home", inclusive = false)
                    },
                    onBack = {
                        compressionViewModel.reset()
                        navController.popBackStack("home", inclusive = false)
                    }
                )
            } ?: LaunchedEffect(Unit) { navController.popBackStack() }
        }
        
        composable("settings") {
            SettingsScreen(
                currentDefaultMode = defaultMode,
                onDefaultModeChange = settingsViewModel::setDefaultMode,
                currentDefaultFormat = defaultFormat,
                onDefaultFormatChange = settingsViewModel::setDefaultFormat,
                onAboutClick = { navController.navigate("about") },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("about") {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
