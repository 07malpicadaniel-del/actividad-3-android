package com.example.taskapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskapp.data.ScheduleSlot
import com.example.taskapp.ui.viewmodel.RoutineType
import com.example.taskapp.ui.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(viewModel: TaskViewModel) {
    val selectedDay by viewModel.selectedScheduleDay.collectAsState()
    val scheduleSlots by viewModel.scheduleSlotsForSelectedDay.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var showRoutineDialog by remember { mutableStateOf(false) }
    var slotToEdit by remember { mutableStateOf<ScheduleSlot?>(null) }

    val daysOfWeek = listOf(
        Pair(1, "Lunes"),
        Pair(2, "Martes"),
        Pair(3, "Miércoles"),
        Pair(4, "Jueves"),
        Pair(5, "Viernes"),
        Pair(6, "Sábado"),
        Pair(7, "Domingo")
    )

    val completedCount = scheduleSlots.count { it.isCompleted }
    val totalCount = scheduleSlots.size

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallFloatingActionButton(
                    onClick = { showRoutineDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Cargar Rutina Predeterminada")
                }

                FloatingActionButton(
                    onClick = {
                        slotToEdit = null
                        showDialog = true
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Bloque de Horario")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Day Selector Tabs
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedDay - 1,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                daysOfWeek.forEach { (dayNum, dayName) ->
                    Tab(
                        selected = selectedDay == dayNum,
                        onClick = { viewModel.selectScheduleDay(dayNum) },
                        text = { Text(dayName, fontWeight = if (selectedDay == dayNum) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            // Quick Preset Routine Banner Button & Daily Completion Summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Actividades del Día", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    if (totalCount > 0) {
                        Text("$completedCount de $totalCount cumplidos", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                OutlinedButton(onClick = { showRoutineDialog = true }) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cargar Rutina", fontSize = 12.sp)
                }
            }

            // Time Slots List
            Box(modifier = Modifier.weight(1f)) {
                if (scheduleSlots.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sin actividades en el horario para este día.\nPresiona 'Cargar Rutina' o + para organizar tu tiempo.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(scheduleSlots, key = { it.id }) { slot ->
                            ScheduleSlotCard(
                                slot = slot,
                                onToggleComplete = { viewModel.toggleScheduleSlotCompleted(slot) },
                                onEdit = {
                                    slotToEdit = slot
                                    showDialog = true
                                },
                                onDelete = { viewModel.deleteScheduleSlot(slot) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRoutineDialog) {
        RoutinePresetDialog(
            onDismiss = { showRoutineDialog = false },
            onSelectRoutine = { routineType ->
                viewModel.applyPredefinedRoutine(selectedDay, routineType)
                showRoutineDialog = false
            }
        )
    }

    if (showDialog) {
        ScheduleSlotDialog(
            slot = slotToEdit,
            defaultDayOfWeek = selectedDay,
            onDismiss = { showDialog = false },
            onConfirm = { dayOfWeek, startTime, endTime, title, category, colorHex ->
                if (slotToEdit == null) {
                    viewModel.addScheduleSlot(dayOfWeek, startTime, endTime, title, category, colorHex)
                } else {
                    viewModel.updateScheduleSlot(
                        slotToEdit!!.copy(
                            dayOfWeek = dayOfWeek,
                            startTime = startTime,
                            endTime = endTime,
                            title = title,
                            category = category,
                            colorHex = colorHex
                        )
                    )
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun ScheduleSlotCard(
    slot: ScheduleSlot,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val barColor = remember(slot.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(slot.colorHex))
        } catch (e: Exception) {
            Color(0xFF2196F3)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (slot.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox to mark as accomplished
            Checkbox(
                checked = slot.isCompleted,
                onCheckedChange = { onToggleComplete() }
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Color Bar Indicator
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Time Range
                Text(
                    text = "${slot.startTime} - ${slot.endTime}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Title
                Text(
                    text = slot.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (slot.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (slot.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Category Tag
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = slot.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar Horario")
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar Horario",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun RoutinePresetDialog(
    onDismiss: () -> Unit,
    onSelectRoutine: (RoutineType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cargar Rutina Predeterminada") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Selecciona una plantilla para aplicar actividades estándar al día seleccionado:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                RoutineType.entries.forEach { routine ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectRoutine(routine) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(routine.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(routine.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleSlotDialog(
    slot: ScheduleSlot? = null,
    defaultDayOfWeek: Int = 1,
    onDismiss: () -> Unit,
    onConfirm: (dayOfWeek: Int, startTime: String, endTime: String, title: String, category: String, colorHex: String) -> Unit
) {
    var title by remember { mutableStateOf(slot?.title ?: "") }
    var startTime by remember { mutableStateOf(slot?.startTime ?: "08:00") }
    var endTime by remember { mutableStateOf(slot?.endTime ?: "09:00") }
    var category by remember { mutableStateOf(slot?.category ?: "General") }
    var selectedDay by remember { mutableIntStateOf(slot?.dayOfWeek ?: defaultDayOfWeek) }
    var selectedColorHex by remember { mutableStateOf(slot?.colorHex ?: "#2196F3") }

    val daysMap = listOf(
        Pair(1, "Lun"),
        Pair(2, "Mar"),
        Pair(3, "Mié"),
        Pair(4, "Jue"),
        Pair(5, "Vie"),
        Pair(6, "Sáb"),
        Pair(7, "Dom")
    )

    val colorOptions = listOf("#2196F3", "#4CAF50", "#FF9800", "#9C27B0", "#E91E63", "#00BCD4")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (slot == null) "Nuevo Bloque de Horario" else "Editar Bloque de Horario") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Actividad / Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Hora Inicio (ej. 08:00)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Hora Fin (ej. 09:30)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Día de la Semana:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(daysMap) { (dayNum, dayLabel) ->
                        FilterChip(
                            selected = selectedDay == dayNum,
                            onClick = { selectedDay = dayNum },
                            label = { Text(dayLabel) }
                        )
                    }
                }

                Text("Categoría:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(listOf("General", "Trabajo", "Estudios", "Ejercicio", "Almuerzo", "Descanso")) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Text("Color Identificador:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.forEach { hex ->
                        val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Blue }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(color)
                                .clickable { selectedColorHex = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedDay, startTime, endTime, title, category, selectedColorHex) },
                enabled = title.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
