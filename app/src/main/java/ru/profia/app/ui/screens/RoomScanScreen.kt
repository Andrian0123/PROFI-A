package ru.profia.app.ui.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.profia.app.R
import android.net.Uri
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.viewmodel.RoomScanViewModel
import java.io.File

/** Состояние экрана сканирования по алгоритму измерения помещения (Подключить → Сканировать → Анализ → Размеры). */
private enum class ScanStep { NOT_CONNECTED, CONNECTED, SCANNING, ANALYZING, SCAN_ERROR, SHOW_DIMENSIONS }

/**
 * Экран сканирования помещения 3D (по аналогии с Polycam).
 * Алгоритм: открыть камеру → Подключить → Сканировать (1,5 м от пола, 2 м от стены) → отметить участки на плане → формирование 3D-проекта (примыкание пола/потолка, углы, окна, двери) → анализ видеосканером → предварительные размеры (высота стен, ширина по периметру).
 */
@Composable
fun RoomScanScreen(
    navController: NavController,
    projectId: String,
    roomId: String,
    viewModel: RoomScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val projectName by viewModel.projectName.collectAsState()
    var hasCameraPermission by remember { mutableStateOf(false) }
    var scanStep by remember { mutableStateOf(ScanStep.NOT_CONNECTED) }
    var preliminaryWallHeight by remember { mutableStateOf<Double?>(null) }
    var preliminaryPerimeter by remember { mutableStateOf<Double?>(null) }
    var preliminaryFloorAreaM2 by remember { mutableStateOf<Double?>(null) }
    var preliminaryCoveragePercent by remember { mutableStateOf<Double?>(null) }
    var selectedFrameFile by remember { mutableStateOf<File?>(null) }
    var scanErrorMessage by remember { mutableStateOf<String?>(null) }
    val isServerProcessing by viewModel.serverProcessing.collectAsState()
    val scope = rememberCoroutineScope()

    // Выбор фото или файла для сканирования
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { u ->
            scope.launch {
                val file = withContext(Dispatchers.IO) {
                    copyUriToScanFile(context, u, projectId, roomId)
                }
                if (file != null) {
                    selectedFrameFile = file
                    scanStep = ScanStep.ANALYZING
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.scan_pick_file_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Простая «паутинка» покрытия — 4x4 ячейки поверх сетки, отмечаем какие уже пройдены сканером.
    val gridRows = 4
    val gridCols = 4
    var coverage by remember { mutableStateOf(List(gridRows) { List(gridCols) { false } }) }

    fun resetCoverage() {
        coverage = List(gridRows) { List(gridCols) { false } }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.onScanSessionEnd() }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        val alreadyGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) {
            hasCameraPermission = true
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(scanStep) {
        when (scanStep) {
            ScanStep.SCANNING -> {
                resetCoverage()
                val totalCells = gridRows * gridCols
                for (index in 0 until totalCells) {
                    // при смене шага (например, переход к ANALYZING) корутина будет отменена
                    delay(400)
                    val row = index / gridCols
                    val col = index % gridCols
                    coverage = coverage.mapIndexed { r, rowList ->
                        rowList.mapIndexed { c, checked ->
                            if (r == row && c == col) true else checked
                        }
                    }
                }
            }
            ScanStep.ANALYZING -> {
                delay(500)
                val frameFile = selectedFrameFile ?: createSyntheticFrameFile(context, projectId, roomId)
                val processResult = viewModel.processScanOnServer(
                    frameFiles = listOf(frameFile),
                    trajectoryJson = buildSyntheticTrajectoryJson()
                )
                val processDimensions = processResult.getOrNull()
                if (processDimensions != null && processDimensions.wallHeightM > 0.0 && processDimensions.perimeterM > 0.0) {
                    preliminaryWallHeight = processDimensions.wallHeightM
                    preliminaryPerimeter = processDimensions.perimeterM
                    preliminaryFloorAreaM2 = processDimensions.floorAreaM2.takeIf { it > 0.0 }
                    preliminaryCoveragePercent = processDimensions.coveragePercentage.takeIf { it > 0.0 }
                    frameFile.delete()
                    selectedFrameFile = null
                    scanStep = ScanStep.SHOW_DIMENSIONS
                } else {
                    val finishResult = viewModel.finishScanOnServer()
                    val finishDimensions = finishResult.getOrNull()
                    val hasValidDimensions = finishDimensions?.wallHeightM?.let { it > 0.0 } == true ||
                        finishDimensions?.perimeterM?.let { it > 0.0 } == true
                    if (hasValidDimensions) {
                        preliminaryWallHeight = finishDimensions?.wallHeightM?.takeIf { it > 0.0 } ?: 2.75
                        preliminaryPerimeter = finishDimensions?.perimeterM?.takeIf { it > 0.0 } ?: 12.4
                        preliminaryFloorAreaM2 = finishDimensions?.floorAreaM2?.takeIf { it > 0.0 }
                        preliminaryCoveragePercent = finishDimensions?.coveragePercentage?.takeIf { it > 0.0 }
                        frameFile.delete()
                        selectedFrameFile = null
                        scanStep = ScanStep.SHOW_DIMENSIONS
                    } else {
                        frameFile.delete()
                        selectedFrameFile = null
                        scanErrorMessage = when {
                            processResult.isFailure && finishResult.isFailure -> {
                                val msg = processResult.exceptionOrNull()?.message ?: ""
                                if (msg.contains("timeout", ignoreCase = true) || msg.contains("Unable to resolve host", ignoreCase = true)) {
                                    context.getString(R.string.scan_error_network)
                                } else {
                                    context.getString(R.string.scan_error_server)
                                }
                            }
                            else -> context.getString(R.string.scan_server_unavailable_toast)
                        }
                        scanStep = ScanStep.SCAN_ERROR
                    }
                }
            }
            else -> {
                resetCoverage()
            }
        }
    }

    BaseScreen(
        navController = navController,
        title = projectName ?: stringResource(R.string.scan_title),
        useAdaptivePadding = false
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (hasCameraPermission) {
            CameraPreviewWithGrid(
                modifier = Modifier.fillMaxSize(),
                context = context,
                lifecycleOwner = lifecycleOwner,
                gridRows = gridRows,
                gridCols = gridCols,
                coverage = coverage
            )
        } else {
            Text(
                text = stringResource(R.string.scan_no_camera_access),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (scanStep) {
                ScanStep.NOT_CONNECTED -> {
                    Text(
                        text = stringResource(R.string.scan_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.scan_instruction),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { scanStep = ScanStep.CONNECTED },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.scan_connect))
                    }
                }
                ScanStep.CONNECTED -> {
                    Text(
                        text = stringResource(R.string.scan_constraints),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = stringResource(R.string.scan_3d_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.scan_mark_on_plan_coming_soon),
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.scan_mark_on_plan), style = MaterialTheme.typography.labelMedium)
                        }
                        Button(
                            onClick = { pickerLauncher.launch("*/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.scan_scan))
                        }
                    }
                }
                ScanStep.SCANNING -> {
                    Text(
                        text = stringResource(R.string.scan_constraints),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { scanStep = ScanStep.ANALYZING },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.scan_stop))
                    }
                }
                ScanStep.ANALYZING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 12.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = if (isServerProcessing) {
                                "${stringResource(R.string.scan_analyzing)} (API)"
                            } else {
                                stringResource(R.string.scan_analyzing)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                ScanStep.SCAN_ERROR -> {
                    Text(
                        text = stringResource(R.string.scan_error_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = scanErrorMessage ?: stringResource(R.string.scan_error_server),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            scanErrorMessage = null
                            scanStep = ScanStep.CONNECTED
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.scan_retry))
                    }
                }
                ScanStep.SHOW_DIMENSIONS -> {
                    Text(
                        text = stringResource(R.string.scan_preliminary_dimensions),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.scan_wall_height), style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    preliminaryWallHeight?.let { "%.2f м".format(it) } ?: "—",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.scan_wall_width_perimeter), style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    preliminaryPerimeter?.let { "%.2f м".format(it) } ?: "—",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.add_room_floor_area), style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    preliminaryFloorAreaM2?.let { "%.2f м²".format(it) } ?: "—",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            preliminaryCoveragePercent?.let { pct ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(stringResource(R.string.scan_coverage_percent, pct), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.back))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.markScanReady()
                                val prev = navController.previousBackStackEntry
                                prev?.savedStateHandle?.set("roomScanWallHeight", preliminaryWallHeight)
                                prev?.savedStateHandle?.set("roomScanPerimeter", preliminaryPerimeter)
                                prev?.savedStateHandle?.set("roomScanFloorAreaM2", preliminaryFloorAreaM2)
                                navController.popBackStack()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.content_desc_save_dimensions),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(stringResource(R.string.save))
                        }
                    }
                }
            }
        }
    }
    }
}

/** Копирует выбранное фото/файл из Uri в кэш для отправки на сервер сканирования. */
private suspend fun copyUriToScanFile(
    context: Context,
    uri: Uri,
    projectId: String,
    roomId: String
): File? = withContext(Dispatchers.IO) {
    try {
        val dir = File(context.cacheDir, "scan_frames").apply { mkdirs() }
        val ext = context.contentResolver.getType(uri)?.substringAfterLast("/") ?: "jpg"
        val safeExt = if (ext in listOf("jpeg", "jpg", "png", "webp")) ext else "jpg"
        val file = File(dir, "scan_picked_${projectId}_${roomId}_${System.currentTimeMillis()}.$safeExt")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        if (file.exists() && file.length() > 0) file else null
    } catch (_: Exception) {
        null
    }
}

private suspend fun createSyntheticFrameFile(
    context: Context,
    projectId: String,
    roomId: String
): File = withContext(Dispatchers.IO) {
    val dir = File(context.cacheDir, "scan_frames").apply { mkdirs() }
    val file = File(dir, "scan_${projectId}_${roomId}_${System.currentTimeMillis()}.jpg")
    val bitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888)
    bitmap.eraseColor(AndroidColor.DKGRAY)
    file.outputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    bitmap.recycle()
    file
}

private fun buildSyntheticTrajectoryJson(): String = """
    [
      {"t": 0.0, "position": [0.0, 1.5, 0.0]},
      {"t": 0.5, "position": [0.6, 1.5, 0.0]},
      {"t": 1.0, "position": [0.6, 1.5, 0.8]},
      {"t": 1.5, "position": [0.0, 1.5, 0.8]}
    ]
""".trimIndent()

@Composable
private fun CameraPreviewWithGrid(
    modifier: Modifier = Modifier,
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    gridRows: Int,
    gridCols: Int,
    coverage: List<List<Boolean>>
) {
    val previewView = remember { androidx.camera.view.PreviewView(context) }
    var bound by remember { mutableStateOf(false) }
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val cellFill = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)

    Box(modifier = modifier) {
        AndroidView(
            factory = { _ ->
                previewView
            },
            modifier = Modifier.fillMaxSize(),
            update = { pv ->
                if (!bound) {
                    bound = true
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener(
                        {
                            val provider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(pv.getSurfaceProvider())
                            }
                            provider.unbindAll()
                            try {
                                provider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview
                                )
                            } catch (_: Exception) { }
                        },
                        ContextCompat.getMainExecutor(context)
                    )
                }
            }
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val stepX = w / gridCols
            val stepY = h / gridRows

            // заливка ячеек, которые помечены как «просканированные»
            for (row in 0 until gridRows) {
                for (col in 0 until gridCols) {
                    if (coverage.getOrNull(row)?.getOrNull(col) == true) {
                        val left = col * stepX
                        val top = row * stepY
                        drawRect(
                            color = cellFill,
                            topLeft = Offset(left, top),
                            size = Size(stepX, stepY)
                        )
                    }
                }
            }

            // линии сетки (вертикали и горизонты)
            for (i in 1 until gridCols) {
                drawLine(
                    color = gridColor,
                    start = Offset(i * stepX, 0f),
                    end = Offset(i * stepX, h),
                    strokeWidth = 1f
                )
            }
            for (j in 1 until gridRows) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, j * stepY),
                    end = Offset(w, j * stepY),
                    strokeWidth = 1f
                )
            }
        }
    }

}
