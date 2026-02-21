package ru.profia.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.io.File
import ru.profia.app.data.model.OpeningFormData
import ru.profia.app.data.model.OpeningType
import ru.profia.app.data.model.RoomFormData
import ru.profia.app.data.model.RoomWorkItemForm
import ru.profia.app.data.model.SuggestedWorkItem
import ru.profia.app.ui.components.BaseScreen
import ru.profia.app.ui.components.AddOpeningDialog
import ru.profia.app.ui.components.LoadingIndicator
import ru.profia.app.ui.components.ProfiTextField
import ru.profia.app.ui.viewmodel.AddRoomViewModel
import ru.profia.app.ui.components.RoundedButton
import ru.profia.app.ui.components.UnsavedChangesDialog
import ru.profia.app.R
import ru.profia.app.ui.navigation.NavRoutes
import androidx.navigation.NavController

/**
 * Экран добавления/редактирования комнаты.
 */

// Подсказки названий комнат из общего справочника видов комнат
private val ROOM_NAME_SUGGESTIONS: List<String> get() = ru.profia.app.data.reference.RoomTypesReference.all

/** Шаблоны единиц измерения (сокращённо): погонный метр, кв.метр, штук, куб.метр, точка. */
private val WORK_UNIT_SUGGESTIONS = listOf(
    "п.м.",   // погонный метр
    "кв.м.",  // квадратный метр
    "шт.",    // штук
    "м³",     // кубический метр
    "т."     // точка
)

private val WORK_TYPE_SECTIONS = listOf(
    "Потолок",
    "Стены",
    "Пол",
    "Сантехника",
    "Электрика",
    "Двери",
    "Окна",
    "Вентиляция",
    "Прочие работы"
)

private data class WorkItem(
    val name: String,
    val unitAbbr: String,
    val price: Double,
    val quantity: Double
) {
    val total: Double get() = price * quantity
}

@Composable
fun AddRoomScreen(
    navController: NavController,
    projectId: String,
    roomId: String?,
    addRoomViewModel: AddRoomViewModel,
    onSave: (RoomFormData, List<OpeningFormData>, Map<String, List<RoomWorkItemForm>>) -> Unit,
    onMenuClick: (() -> Unit)? = null,
    isReadOnly: Boolean = false
) {
    val isEditMode = roomId != null
    val canEdit = !isReadOnly
    val projectName by addRoomViewModel.projectName.collectAsState()
    val initialForm by addRoomViewModel.initialForm.collectAsState()
    val initialOpenings by addRoomViewModel.initialOpenings.collectAsState()
    val initialWorkItems by addRoomViewModel.initialWorkItems.collectAsState(initial = null)
    val workTemplates by addRoomViewModel.workTemplates.collectAsState()
    val suggestedWorkItems by addRoomViewModel.suggestedWorkItems.collectAsState()
    var usedSuggestedKeys by remember { mutableStateOf(setOf<String>()) }
    fun suggestedKey(item: SuggestedWorkItem) =
        "${item.category}|${item.name}|${item.unitAbbr}|${item.price}"
    var roomName by remember { mutableStateOf("") }
    var length by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var floorAreaStr by remember { mutableStateOf("") }
    var ceilingAreaStr by remember { mutableStateOf("") }
    var wallAreaStr by remember { mutableStateOf("") }
    var hasSlopes by remember { mutableStateOf(false) }
    var slopesLength by remember { mutableStateOf("") }
    var hasBoxes by remember { mutableStateOf(false) }
    var boxesLength by remember { mutableStateOf("") }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showAddOpeningDialog by remember { mutableStateOf(false) }
    var showPhoto3DDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Сканировать: выбор фото или файла 3D; после интеграции с сервисом распознавания — подстановка размеров (длина, ширина, высота) в поля комнаты
    val load3DLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                val dir = File(context.filesDir, "room_3d").apply { mkdirs() }
                val file = File(dir, "${projectId}_${roomId ?: "new"}_${System.currentTimeMillis()}.obj")
                val ok = withContext(Dispatchers.IO) {
                    try {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            file.outputStream().use { output -> input.copyTo(output) }
                        }
                        true
                    } catch (_: Exception) { false }
                }
                if (ok) {
                    addRoomViewModel.saveMeshPath(projectId, roomId ?: "new", file.absolutePath)
                    Toast.makeText(context, context.getString(R.string.add_room_toast_file_saved), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, context.getString(R.string.add_room_toast_copy_error), Toast.LENGTH_SHORT).show()
                }
                showPhoto3DDialog = false
            }
        }
    }
    var showCalculator by remember { mutableStateOf(false) }
    var expandedRoomParams by remember { mutableStateOf(false) }
    var isFloorMode by remember { mutableStateOf(false) }
    var expandedWorkSection by remember { mutableStateOf<String?>(null) }
    var workName by remember { mutableStateOf("") }
    var workUnit by remember { mutableStateOf("кв.м.") }
    var workPrice by remember { mutableStateOf("") }
    var workQty by remember { mutableStateOf("") }
    var showUnitDropdown by remember { mutableStateOf(false) }
    var editingSectionName by remember { mutableStateOf<String?>(null) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    val workItemsByCategory = remember {
        mutableStateMapOf<String, MutableList<WorkItem>>().apply {
            WORK_TYPE_SECTIONS.forEach { put(it, mutableStateListOf()) }
        }
    }
    val openings = remember { mutableStateListOf<OpeningFormData>() }

    val lengthNum = length.replace(",", ".").toDoubleOrNull() ?: 0.0
    val widthNum = width.replace(",", ".").toDoubleOrNull() ?: 0.0
    val heightNum = height.replace(",", ".").toDoubleOrNull() ?: 0.0

    val floorArea = lengthNum * widthNum
    val baseWallArea = if (heightNum > 0) 2 * (lengthNum + widthNum) * heightNum else 0.0
    val openingsArea = openings.sumOf { it.width * it.height * it.count }
    val wallArea = (baseWallArea - openingsArea).coerceAtLeast(0.0)
    val ceilingArea = floorArea
    val floorPerimeter = 2 * (lengthNum + widthNum)
    val ceilingPerimeter = floorPerimeter
    val slopesLengthNum = slopesLength.replace(",", ".").toDoubleOrNull() ?: 0.0
    val boxesLengthNum = boxesLength.replace(",", ".").toDoubleOrNull() ?: 0.0
    val hasUnsavedChanges = roomName.isNotBlank() || length.isNotBlank() || width.isNotBlank() ||
        height.isNotBlank() || floorAreaStr.isNotBlank() || ceilingAreaStr.isNotBlank() || wallAreaStr.isNotBlank() ||
        slopesLength.isNotBlank() || boxesLength.isNotBlank()

    val effectiveFloorArea = floorAreaStr.replace(",", ".").toDoubleOrNull()?.takeIf { it > 0 } ?: floorArea
    val effectiveCeilingArea = ceilingAreaStr.replace(",", ".").toDoubleOrNull()?.takeIf { it > 0 } ?: ceilingArea
    val effectiveWallArea = wallAreaStr.replace(",", ".").toDoubleOrNull()?.takeIf { it > 0 } ?: wallArea

    var hasAppliedInitial by remember { mutableStateOf(false) }
    LaunchedEffect(initialForm) {
        if (isEditMode && !hasAppliedInitial && initialForm != null) {
            val f = initialForm!!
            roomName = f.name
            length = if (f.length > 0) "%.2f".format(f.length).trimEnd('0').trimEnd('.') else ""
            width = if (f.width > 0) "%.2f".format(f.width).trimEnd('0').trimEnd('.') else ""
            height = if (f.height > 0) "%.2f".format(f.height).trimEnd('0').trimEnd('.') else ""
            floorAreaStr = f.floorAreaOverride?.let { if (it > 0) "%.2f".format(it).trimEnd('0').trimEnd('.') else "" } ?: ""
            ceilingAreaStr = f.ceilingAreaOverride?.let { if (it > 0) "%.2f".format(it).trimEnd('0').trimEnd('.') else "" } ?: ""
            wallAreaStr = f.wallAreaOverride?.let { if (it > 0) "%.2f".format(it).trimEnd('0').trimEnd('.') else "" } ?: ""
            hasSlopes = f.hasSlopes
            slopesLength = if (f.slopesLength > 0) "%.2f".format(f.slopesLength).trimEnd('0').trimEnd('.') else ""
            hasBoxes = f.hasBoxes
            boxesLength = if (f.boxesLength > 0) "%.2f".format(f.boxesLength).trimEnd('0').trimEnd('.') else ""
            hasAppliedInitial = true
        }
    }
    var hasAppliedOpenings by remember { mutableStateOf(false) }
    LaunchedEffect(initialOpenings) {
        if (isEditMode && !hasAppliedOpenings && initialOpenings.isNotEmpty()) {
            openings.clear()
            openings.addAll(initialOpenings)
            hasAppliedOpenings = true
        }
    }
    var hasAppliedWorkItems by remember { mutableStateOf(false) }
    LaunchedEffect(initialWorkItems) {
        if (isEditMode && !hasAppliedWorkItems && initialWorkItems != null) {
            initialWorkItems!!.forEach { (category, list) ->
                val targetList = workItemsByCategory.getOrPut(category) { mutableStateListOf() }
                targetList.clear()
                list.forEach { form ->
                    targetList.add(WorkItem(form.name, form.unitAbbr, form.price, form.quantity))
                }
            }
            hasAppliedWorkItems = true
        }
    }

    // Результаты 3D‑сканирования: применить к комнате высоту, периметр и площадь пола
    var scanPerimeter by remember { mutableStateOf<Double?>(null) }
    LaunchedEffect(navController.currentBackStackEntry) {
        val handle = navController.currentBackStackEntry?.savedStateHandle
        val wallHeightFromScan = handle?.get<Double>("roomScanWallHeight")
        val perimeterFromScan = handle?.get<Double>("roomScanPerimeter")
        val floorAreaFromScan = handle?.get<Double>("roomScanFloorAreaM2")
        if (wallHeightFromScan != null) {
            height = "%.2f".format(wallHeightFromScan).trimEnd('0').trimEnd('.')
            handle.remove<Double>("roomScanWallHeight")
        }
        if (perimeterFromScan != null) {
            scanPerimeter = perimeterFromScan
            handle.remove<Double>("roomScanPerimeter")
        }
        if (floorAreaFromScan != null && floorAreaFromScan > 0.0) {
            floorAreaStr = "%.2f".format(floorAreaFromScan).trimEnd('0').trimEnd('.')
            handle.remove<Double>("roomScanFloorAreaM2")
        }
    }

    var openingsModified by remember { mutableStateOf(false) }
    BackHandler(enabled = canEdit && (hasUnsavedChanges || openingsModified)) {
        showUnsavedDialog = true
    }

    BaseScreen(
        navController = navController,
        title = projectName ?: "",
        showBackButton = true,
        showSaveButton = canEdit,
        saveAsCheckIcon = true,
        onMenuClick = onMenuClick,
        onBackClick = if (canEdit && (hasUnsavedChanges || openingsModified)) { { showUnsavedDialog = true } } else null,
        modifier = Modifier.testTag("add_room_screen"),
        onSave = {
            val workItemsMap = workItemsByCategory.mapValues { (cat, list) ->
                list.map { RoomWorkItemForm(cat, it.name, it.unitAbbr, it.price, it.quantity) }
            }
            onSave(
                RoomFormData(
                    name = roomName,
                    length = lengthNum,
                    width = widthNum,
                    height = heightNum,
                    floorAreaOverride = floorAreaStr.replace(",", ".").toDoubleOrNull()?.takeIf { it > 0 },
                    ceilingAreaOverride = ceilingAreaStr.replace(",", ".").toDoubleOrNull()?.takeIf { it > 0 },
                    wallAreaOverride = wallAreaStr.replace(",", ".").toDoubleOrNull()?.takeIf { it > 0 },
                    hasSlopes = hasSlopes,
                    slopesLength = slopesLength.replace(",", ".").toDoubleOrNull() ?: 0.0,
                    hasBoxes = hasBoxes,
                    boxesLength = boxesLength.replace(",", ".").toDoubleOrNull() ?: 0.0
                ),
                openings,
                workItemsMap
            )
        },
        isSaveEnabled = canEdit && roomName.isNotBlank() && lengthNum > 0 && widthNum > 0,
        actions = {
            IconButton(onClick = { showCalculator = true }, enabled = canEdit) {
                Icon(
                    Icons.Default.Calculate,
                    contentDescription = stringResource(R.string.content_desc_calculator),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { paddingValues ->
        if (isEditMode && initialForm == null) {
            LoadingIndicator()
        } else {
        Box(modifier = Modifier.fillMaxWidth()) {
            val scrollState = rememberScrollState()
            val focusManager = LocalFocusManager.current
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .then(
                        if (showPhoto3DDialog) Modifier.pointerInput(Unit) {
                            detectVerticalDragGestures { _, _ -> /* блокируем скролл фона при открытом диалоге Фото 3D */ }
                        } else Modifier
                    )
                    .verticalScroll(scrollState)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when {
                            isEditMode -> stringResource(R.string.edit_room)
                            isFloorMode -> stringResource(R.string.add_room_add_floor)
                            else -> stringResource(R.string.add_room)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showCalculator = true },
                        enabled = canEdit,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = stringResource(R.string.content_desc_calculator),
                            tint = if (canEdit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                }
                ProfiTextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    label = stringResource(R.string.add_room_room_name_label),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                    readOnly = isReadOnly
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { expandedRoomParams = !expandedRoomParams },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.add_room_edit_params),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(
                                imageVector = if (expandedRoomParams) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { showPhoto3DDialog = true },
                        enabled = canEdit,
                        modifier = Modifier.padding(0.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp).padding(end = 4.dp)
                        )
                        Text(stringResource(R.string.add_room_photo_3d), style = MaterialTheme.typography.labelLarge)
                    }
                }
                AnimatedVisibility(visible = expandedRoomParams) {
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ProfiTextField(
                                value = length,
                                onValueChange = { length = it },
                                label = stringResource(R.string.add_room_length),
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                                readOnly = isReadOnly
                            )
                            ProfiTextField(
                                value = width,
                                onValueChange = { width = it },
                                label = stringResource(R.string.add_room_width),
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                                readOnly = isReadOnly
                            )
                            ProfiTextField(
                                value = height,
                                onValueChange = { height = it },
                                label = stringResource(R.string.add_room_height),
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                                readOnly = isReadOnly
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ProfiTextField(
                                value = floorAreaStr.ifBlank { if (floorArea > 0) "%.2f".format(floorArea) else "" },
                                onValueChange = { floorAreaStr = it },
                                label = stringResource(R.string.add_room_floor_area),
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                                readOnly = isReadOnly
                            )
                            ProfiTextField(
                                value = ceilingAreaStr.ifBlank { if (ceilingArea > 0) "%.2f".format(ceilingArea) else "" },
                                onValueChange = { ceilingAreaStr = it },
                                label = stringResource(R.string.add_room_ceiling_area),
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                                readOnly = isReadOnly
                            )
                            ProfiTextField(
                                value = wallAreaStr.ifBlank { if (wallArea > 0) "%.2f".format(wallArea) else "" },
                                onValueChange = { wallAreaStr = it },
                                label = stringResource(R.string.add_room_wall_area),
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                                readOnly = isReadOnly
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ProfiTextField(
                                value = slopesLength,
                                onValueChange = { slopesLength = it; hasSlopes = it.trim().isNotEmpty() },
                                label = stringResource(R.string.add_room_slopes_pm),
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                                readOnly = isReadOnly
                            )
                            ProfiTextField(
                                value = boxesLength,
                                onValueChange = { boxesLength = it; hasBoxes = it.trim().isNotEmpty() },
                                label = stringResource(R.string.add_room_boxes_pm),
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                                readOnly = isReadOnly
                            )
                        }
                    }
                }
                // Подсказки по названию комнаты: только варианты, начинающиеся с введённых букв
                val trimmedName = roomName.trim()
                if (trimmedName.isNotEmpty()) {
                    val nameSuggestions = ROOM_NAME_SUGGESTIONS
                        .filter { suggestion ->
                            suggestion.startsWith(trimmedName, ignoreCase = true) &&
                                    !suggestion.equals(trimmedName, ignoreCase = true)
                        }
                    if (nameSuggestions.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        ) {
                            nameSuggestions.take(5).forEach { suggestion ->
                                Text(
                                    text = suggestion,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { roomName = suggestion }
                                        .padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Виды работы — разделы с формой (наименование, ед.изм., цена, количество) и списком добавленных
                Text(
                    stringResource(R.string.add_room_work_types_header),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Кнопки «Добавить откос» и «Добавить короба» удалены по требованию
                    WORK_TYPE_SECTIONS.forEach { sectionName ->
                        val isExpanded = expandedWorkSection == sectionName
                        val items = workItemsByCategory[sectionName] ?: emptyList<WorkItem>()
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val expanding = !isExpanded
                                            expandedWorkSection = if (isExpanded) null else sectionName
                                            if (expanding) {
                                                workQty = when (sectionName) {
                                                    "Потолок" -> if (effectiveCeilingArea > 0) "%.2f".format(effectiveCeilingArea) else ""
                                                    "Пол" -> if (effectiveFloorArea > 0) "%.2f".format(effectiveFloorArea) else ""
                                                    "Стены" -> if (effectiveWallArea > 0) "%.2f".format(effectiveWallArea) else ""
                                                    else -> workQty
                                                }
                                                if (sectionName == "Потолок" || sectionName == "Пол" || sectionName == "Стены") workUnit = "кв.м."
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        sectionName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                AnimatedVisibility(visible = isExpanded) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 0.dp)
                                    ) {
                                        val templatesForSection = workTemplates.filter { it.category == sectionName }
                                        if (templatesForSection.isNotEmpty()) {
                                            Text(
                                                stringResource(R.string.add_room_from_templates),
                                                style = MaterialTheme.typography.labelMedium,
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            )
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState())
                                                    .padding(bottom = 8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                for (t in templatesForSection) {
                                                    TextButton(
                                                        onClick = {
                                                            workName = t.name
                                                            workUnit = t.unitAbbr
                                                            workPrice = if (t.defaultPrice > 0) "%.2f".format(t.defaultPrice) else ""
                                                        }
                                                    ) {
                                                        Text(t.name, style = MaterialTheme.typography.bodySmall)
                                                    }
                                                }
                                            }
                                        }
                                        val suggestedForSection = suggestedWorkItems
                                            .filter { it.category == sectionName && suggestedKey(it) !in usedSuggestedKeys }
                                        if (suggestedForSection.isNotEmpty()) {
                                            Text(
                                                stringResource(R.string.add_room_from_other_rooms),
                                                style = MaterialTheme.typography.labelMedium,
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            )
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState())
                                                    .padding(bottom = 8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                suggestedForSection.forEach { item ->
                                                    TextButton(
                                                        onClick = {
                                                            usedSuggestedKeys = usedSuggestedKeys + suggestedKey(item)
                                                            val list = workItemsByCategory[item.category] ?: return@TextButton
                                                            val qty = when (item.category) {
                                                                "Потолок" -> if (effectiveCeilingArea > 0) effectiveCeilingArea else 1.0
                                                                "Стены" -> if (effectiveWallArea > 0) effectiveWallArea else 1.0
                                                                "Пол" -> if (effectiveFloorArea > 0) effectiveFloorArea else 1.0
                                                                else -> 1.0
                                                            }
                                                            list.add(
                                                                WorkItem(
                                                                    name = item.name,
                                                                    unitAbbr = item.unitAbbr,
                                                                    price = item.price,
                                                                    quantity = qty
                                                                )
                                                            )
                                                        },
                                                        modifier = Modifier
                                                            .border(
                                                                1.dp,
                                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                                                                RoundedCornerShape(8.dp)
                                                            )
                                                    ) {
                                                        Text(
                                                            text = "${item.name} · ${"%.0f".format(item.price)} ₽",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        ProfiTextField(
                                            value = workName,
                                            onValueChange = { workName = it },
                                            label = stringResource(R.string.add_room_work_name_label),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                                            readOnly = isReadOnly
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                ProfiTextField(
                                                    value = workUnit,
                                                    onValueChange = { workUnit = it },
                                                    label = stringResource(R.string.add_room_unit_label),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    readOnly = isReadOnly,
                                                    trailingIcon = {
                                                        IconButton(
                                                            onClick = { showUnitDropdown = true },
                                                            enabled = canEdit,
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.ArrowDropDown,
                                                                contentDescription = stringResource(R.string.content_desc_select_unit)
                                                            )
                                                        }
                                                    }
                                                )
                                                DropdownMenu(
                                                    expanded = showUnitDropdown,
                                                    onDismissRequest = { showUnitDropdown = false },
                                                    modifier = Modifier.fillMaxWidth(0.9f)
                                                ) {
                                                    WORK_UNIT_SUGGESTIONS.forEach { unit ->
                                                        DropdownMenuItem(
                                                            text = { Text(unit) },
                                                            onClick = {
                                                                workUnit = unit
                                                                showUnitDropdown = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                            ProfiTextField(
                                                value = workPrice,
                                                onValueChange = { workPrice = it },
                                                label = stringResource(R.string.add_room_price_label),
                                                modifier = Modifier.weight(1f),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                                                readOnly = isReadOnly
                                            )
                                            ProfiTextField(
                                                value = workQty,
                                                onValueChange = { workQty = it },
                                                label = stringResource(R.string.add_room_qty_label),
                                                modifier = Modifier.weight(1f),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                                                readOnly = isReadOnly
                                            )
                                        }
                                        RoundedButton(
                                            text = if (editingSectionName == sectionName && editingIndex != null) stringResource(R.string.save) else stringResource(R.string.add_room_add_btn),
                                            enabled = canEdit,
                                            onClick = {
                                                val priceNum = workPrice.replace(",", ".").toDoubleOrNull()
                                                val qtyNum = workQty.replace(",", ".").toDoubleOrNull()
                                                when {
                                                    workName.isBlank() -> Toast.makeText(context, context.getString(R.string.add_room_validation_name), Toast.LENGTH_SHORT).show()
                                                    workUnit.isBlank() -> Toast.makeText(context, context.getString(R.string.add_room_validation_unit), Toast.LENGTH_SHORT).show()
                                                    workPrice.isBlank() -> Toast.makeText(context, context.getString(R.string.add_room_validation_price), Toast.LENGTH_SHORT).show()
                                                    priceNum == null -> Toast.makeText(context, context.getString(R.string.add_room_validation_price_invalid), Toast.LENGTH_SHORT).show()
                                                    workQty.isBlank() -> Toast.makeText(context, context.getString(R.string.add_room_validation_qty), Toast.LENGTH_SHORT).show()
                                                    qtyNum == null || qtyNum < 0 -> Toast.makeText(context, context.getString(R.string.add_room_validation_qty_invalid), Toast.LENGTH_SHORT).show()
                                                    else -> {
                                                        val newItem = WorkItem(
                                                            name = workName.trim(),
                                                            unitAbbr = workUnit.trim(),
                                                            price = priceNum,
                                                            quantity = qtyNum
                                                        )
                                                        val list = workItemsByCategory[sectionName]
                                                        if (editingSectionName == sectionName && editingIndex != null && list != null && editingIndex!! < list.size) {
                                                            list[editingIndex!!] = newItem
                                                            editingSectionName = null
                                                            editingIndex = null
                                                        } else {
                                                            list?.add(newItem)
                                                        }
                                                        workName = ""
                                                        workUnit = when (sectionName) {
                                                            "Потолок", "Стены", "Пол" -> "кв.м."
                                                            else -> ""
                                                        }
                                                        workPrice = ""
                                                        workQty = when (sectionName) {
                                                            "Потолок" -> if (effectiveCeilingArea > 0) "%.2f".format(effectiveCeilingArea) else ""
                                                            "Стены" -> if (effectiveWallArea > 0) "%.2f".format(effectiveWallArea) else ""
                                                            "Пол" -> if (effectiveFloorArea > 0) "%.2f".format(effectiveFloorArea) else ""
                                                            else -> ""
                                                        }
                                                        focusManager.clearFocus(true)
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        )
                                        items.forEachIndexed { index, item ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 6.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            item.name,
                                                            style = MaterialTheme.typography.bodyMedium
                                                        )
                                                        Text(
                                                            "${"%.2f".format(item.quantity)} ${item.unitAbbr}, ${"%.2f".format(item.price)} ₽",
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                        Text(
                                                            "${"%.2f".format(item.total)} ₽",
                                                            style = MaterialTheme.typography.titleSmall
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            workName = item.name
                                                            workUnit = item.unitAbbr
                                                            workPrice = if (item.price > 0) "%.2f".format(item.price) else ""
                                                            workQty = if (item.quantity > 0) "%.2f".format(item.quantity) else ""
                                                            editingSectionName = sectionName
                                                            editingIndex = index
                                                        },
                                                        enabled = canEdit
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Edit,
                                                            contentDescription = stringResource(R.string.content_desc_edit)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                }
            }
        }

        OutlinedButton(
            onClick = { showAddOpeningDialog = true; openingsModified = true },
            enabled = canEdit,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text(stringResource(R.string.add_opening_title))
        }
        if (openings.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(openings.size, key = { it }) { index ->
                    val o = openings[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(
                                    R.string.add_opening_summary,
                                    if (o.type == OpeningType.DOOR) stringResource(R.string.add_opening_door) else stringResource(R.string.add_opening_window),
                                    o.width,
                                    o.height,
                                    o.count
                                )
                            )
                            IconButton(
                                onClick = { openings.removeAt(index); openingsModified = true },
                                enabled = canEdit
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.content_desc_delete))
                            }
                        }
                    }
                }
            }
        }

        val totalRoomSum = workItemsByCategory.values.sumOf { list -> list.sumOf { it.total } }

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(stringResource(R.string.add_room_calculations), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.add_room_floor_area_value, effectiveFloorArea))
                Text(stringResource(R.string.add_room_ceiling_area_value, effectiveCeilingArea))
                if (openings.isNotEmpty()) {
                    Text(stringResource(R.string.add_room_openings_area_value, openingsArea), style = MaterialTheme.typography.bodyMedium)
                }
                Text(stringResource(R.string.add_room_wall_area_value, effectiveWallArea))
                if (lengthNum > 0 && widthNum > 0) {
                    Text(stringResource(R.string.add_room_perimeter_floor_value, floorPerimeter))
                    Text(stringResource(R.string.add_room_perimeter_ceiling_value, ceilingPerimeter))
                }
                if (hasSlopes && slopesLengthNum > 0.0) {
                    Text(stringResource(R.string.add_room_slopes_value, slopesLengthNum))
                }
                if (hasBoxes && boxesLengthNum > 0.0) {
                    Text(stringResource(R.string.add_room_boxes_value, boxesLengthNum))
                }
                Text(
                    stringResource(R.string.add_room_total_sum_value, totalRoomSum),
                    style = MaterialTheme.typography.titleSmall
                )
                scanPerimeter?.let { p ->
                    Text(
                        stringResource(R.string.add_room_scan_perimeter_value, p),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        }
        if (showCalculator) {
            SimpleCalculatorDialog(onDismiss = { showCalculator = false })
        }
        if (showUnsavedDialog) {
            UnsavedChangesDialog(
                onSave = {
                    if (roomName.isNotBlank() && lengthNum > 0 && widthNum > 0) {
                        val workItemsMap = workItemsByCategory.mapValues { (cat, list) ->
                            list.map { RoomWorkItemForm(cat, it.name, it.unitAbbr, it.price, it.quantity) }
                        }
                        onSave(
                            RoomFormData(
                                name = roomName,
                                length = lengthNum,
                                width = widthNum,
                                height = heightNum,
                                floorAreaOverride = floorAreaStr.replace(",", ".").toDoubleOrNull()?.takeIf { it > 0 },
                                ceilingAreaOverride = ceilingAreaStr.replace(",", ".").toDoubleOrNull()?.takeIf { it > 0 },
                                wallAreaOverride = wallAreaStr.replace(",", ".").toDoubleOrNull()?.takeIf { it > 0 },
                                hasSlopes = hasSlopes,
                                slopesLength = slopesLength.replace(",", ".").toDoubleOrNull() ?: 0.0,
                                hasBoxes = hasBoxes,
                                boxesLength = boxesLength.replace(",", ".").toDoubleOrNull() ?: 0.0
                            ),
                            openings,
                            workItemsMap
                        )
                        showUnsavedDialog = false
                        navController.popBackStack()
                    }
                },
                onDismiss = { showUnsavedDialog = false },
                onExitWithoutSaving = {
                    showUnsavedDialog = false
                    navController.popBackStack()
                }
            )
        }
        if (showAddOpeningDialog) {
            AddOpeningDialog(
                onDismiss = { showAddOpeningDialog = false },
                onAdd = { openings.add(it); openingsModified = true }
            )
        }
        if (showPhoto3DDialog) {
            Dialog(onDismissRequest = { showPhoto3DDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, _ -> /* потребляем скролл, чтобы не уходил под диалог */ }
                        },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Фото 3D комнаты",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        OutlinedButton(
                            onClick = {
                                showPhoto3DDialog = false
                                navController.navigate(NavRoutes.roomScan(projectId, roomId ?: "new"))
                            },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.add_room_photo_3d_create))
                        }
                        OutlinedButton(
                            onClick = { load3DLauncher.launch("*/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.scan_scan))
                        }
                        OutlinedButton(
                            onClick = { load3DLauncher.launch("*/*") },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.add_room_photo_3d_file))
                        }
                    }
                }
            }
        }
        }
    }
    }
}
