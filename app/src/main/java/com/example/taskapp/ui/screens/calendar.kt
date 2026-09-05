package com.example.taskapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskapp.data.Priority
import com.example.taskapp.data.Task
import com.example.taskapp.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(viewModel: TaskViewModel) {
    val selectedDateMillis by viewModel.selectedDateMillis.collectAsState()
    val tasksForDate by viewModel.tasksForSelectedDate.collectAsState()
    val tasksGroupedByDate by viewModel.tasksGroupedByDate.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    // Calendar navigation state (current displayed month)
    var currentMonthCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply { timeInMillis = selectedDateMillis })
    }

    val selectedDateHeaderFormat = remember {
        SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale.getDefault())
    }

    val pendingCount = tasksForDate.count { !it.isCompleted }
    val completedCount = tasksForDate.count { it.isCompleted }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    taskToEdit = null
                    showDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Tarea para la fecha seleccionada")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive Custom Month Calendar Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Month Navigation Header
                    MonthHeader(
                        currentMonth = currentMonthCalendar,
                        onPreviousMonth = {
                            currentMonthCalendar = (currentMonthCalendar.clone() as Calendar).apply {
                                add(Calendar.MONTH, -1)
                            }
                        },
                        onNextMonth = {
                            currentMonthCalendar = (currentMonthCalendar.clone() as Calendar).apply {
                                add(Calendar.MONTH, 1)
                            }
                        },
                        onTodayClick = {
                            val todayMillis = System.currentTimeMillis()
                            currentMonthCalendar = Calendar.getInstance().apply { timeInMillis = todayMillis }
                            viewModel.selectDate(todayMillis)
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Weekday Titles
                    WeekdayHeader()

                    Spacer(modifier = Modifier.height(4.dp))

                    // Month Days Grid with Tasks Inside
                    MonthDaysGrid(
                        displayedMonth = currentMonthCalendar,
                        selectedDateMillis = selectedDateMillis,
                        tasksGroupedByDate = tasksGroupedByDate,
                        onDateSelected = { dateMillis ->
                            viewModel.selectDate(dateMillis)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Summary Header for Selected Date
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = selectedDateHeaderFormat.format(Date(selectedDateMillis))
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$pendingCount pendientes • $completedCount completadas",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Tasks List for Selected Date
            if (tasksForDate.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay tareas programadas para esta fecha.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasksForDate, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            onToggleComplete = { viewModel.toggleTaskCompleted(task) },
                            onEdit = {
                                taskToEdit = task
                                showDialog = true
                            },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        TaskDialog(
            task = taskToEdit,
            defaultDateMillis = selectedDateMillis,
            onDismiss = { showDialog = false },
            onConfirm = { title, description, dateMillis, priority, category ->
                if (taskToEdit == null) {
                    viewModel.addTask(title, description, dateMillis, priority, category)
                } else {
                    viewModel.updateTask(
                        taskToEdit!!.copy(
                            title = title,
                            description = description,
                            dateMillis = dateMillis,
                            priority = priority,
                            category = category
                        )
                    )
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun MonthHeader(
    currentMonth: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit
) {
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val title = monthYearFormat.format(currentMonth.time)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onTodayClick) {
                Icon(Icons.Default.Today, contentDescription = "Hoy")
            }
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mes Anterior")
            }
            IconButton(onClick = onNextMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mes Siguiente")
            }
        }
    }
}

@Composable
fun WeekdayHeader() {
    val days = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
    Row(modifier = Modifier.fillMaxWidth()) {
        days.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MonthDaysGrid(
    displayedMonth: Calendar,
    selectedDateMillis: Long,
    tasksGroupedByDate: Map<Long, List<Task>>,
    onDateSelected: (Long) -> Unit
) {
    val daysInMonth = remember(displayedMonth) {
        val cal = displayedMonth.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val list = mutableListOf<CalendarDay?>()
        for (i in 0 until firstDayOfWeek) {
            list.add(null) // blank spaces before 1st day of month
        }

        for (day in 1..maxDays) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val dayStartMillis = TaskViewModel.getStartOfDay(cal.timeInMillis)
            list.add(CalendarDay(dayNumber = day, startOfDayMillis = dayStartMillis))
        }
        list
    }

    val todayStartMillis = remember { TaskViewModel.getStartOfDay(System.currentTimeMillis()) }
    val selectedStartMillis = remember(selectedDateMillis) { TaskViewModel.getStartOfDay(selectedDateMillis) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        userScrollEnabled = false
    ) {
        items(daysInMonth) { day ->
            if (day == null) {
                Box(modifier = Modifier.size(38.dp))
            } else {
                val isSelected = day.startOfDayMillis == selectedStartMillis
                val isToday = day.startOfDayMillis == todayStartMillis
                val dayTasks = tasksGroupedByDate[day.startOfDayMillis] ?: emptyList()

                DayCell(
                    dayNumber = day.dayNumber,
                    isSelected = isSelected,
                    isToday = isToday,
                    tasks = dayTasks,
                    onClick = { onDateSelected(day.startOfDayMillis) }
                )
            }
        }
    }
}

data class CalendarDay(val dayNumber: Int, val startOfDayMillis: Long)

@Composable
fun DayCell(
    dayNumber: Int,
    isSelected: Boolean,
    isToday: Boolean,
    tasks: List<Task>,
    onClick: () -> Unit
) {
    val pendingTasks = tasks.filter { !it.isCompleted }
    val hasTasks = tasks.isNotEmpty()
    val hasHighPriorityPending = pendingTasks.any { it.priority == Priority.HIGH }

    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier
            .padding(2.dp)
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .then(
                if (isToday && !isSelected) Modifier.border(
                    1.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(8.dp)
                ) else Modifier
            )
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayNumber.toString(),
            fontSize = 13.sp,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )

        // Indicator dots for tasks inside the calendar day
        if (hasTasks) {
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pendingTasks.isNotEmpty()) {
                    val dotColor = if (hasHighPriorityPending) Color(0xFFE53935) else if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                } else {
                    // All completed
                    val dotColor = if (isSelected) Color.White else Color(0xFF4CAF50)
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }
        }
    }
}
