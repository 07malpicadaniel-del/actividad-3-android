package com.example.taskapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskapp.data.Priority
import com.example.taskapp.data.TransactionType
import com.example.taskapp.ui.viewmodel.TaskViewModel
import java.util.Locale

@Composable
fun StatsScreen(viewModel: TaskViewModel) {
    val allTasks by viewModel.allTasks.collectAsState()
    val allFinanceEntries by viewModel.allFinanceEntries.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val allScheduleSlots by viewModel.allScheduleSlots.collectAsState()

    var selectedSectionIndex by remember { mutableIntStateOf(0) }
    val sectionTabs = listOf("Visión General", "Tareas", "Finanzas", "Rutinas / Horario")

    // Task Stats
    val totalTasks = allTasks.size
    val completedTasks = allTasks.count { it.isCompleted }
    val taskCompletionPercentage = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks) * 100f else 0f
    val highPriorityPending = allTasks.count { !it.isCompleted && it.priority == Priority.HIGH }

    // Finance Stats
    val totalExpenses = allFinanceEntries.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val totalIncomeEntries = allFinanceEntries.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalBudgetAvailable = monthlyBudget + totalIncomeEntries
    val remainingBalance = totalBudgetAvailable - totalExpenses
    val spentPercentage = if (totalBudgetAvailable > 0) ((totalExpenses / totalBudgetAvailable) * 100).coerceAtMost(100.0) else 0.0

    val financeCategories = listOf("Renta", "Despensa", "Servicios", "Transporte", "Ocio", "Ahorro", "Otros")
    val expensesByCategory = financeCategories.associateWith { cat ->
        allFinanceEntries.filter { it.type == TransactionType.EXPENSE && it.category.equals(cat, ignoreCase = true) }.sumOf { it.amount }
    }

    // Schedule Stats
    val totalScheduleSlots = allScheduleSlots.size
    val completedScheduleSlots = allScheduleSlots.count { it.isCompleted }
    val scheduleCompliancePercentage = if (totalScheduleSlots > 0) (completedScheduleSlots.toFloat() / totalScheduleSlots) * 100f else 0f

    val scheduleCategories = listOf("Trabajo", "Estudios", "Ejercicio", "Almuerzo", "Descanso", "General")
    val slotsByCategory = scheduleCategories.associateWith { cat ->
        allScheduleSlots.count { it.category.equals(cat, ignoreCase = true) }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        // Section Tabs
        PrimaryTabRow(
            selectedTabIndex = selectedSectionIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            sectionTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSectionIndex == index,
                    onClick = { selectedSectionIndex = index },
                    text = { Text(title, fontSize = 12.sp, fontWeight = if (selectedSectionIndex == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedSectionIndex) {
                0 -> {
                    // Visión General Dashboard
                    Text("Panel Unificado de Control", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryColor)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard(title = "Productividad Tareas", value = "${taskCompletionPercentage.toInt()}%", subtitle = "$completedTasks de $totalTasks tareas", modifier = Modifier.weight(1f))
                        MetricCard(title = "Cumplimiento Horario", value = "${scheduleCompliancePercentage.toInt()}%", subtitle = "$completedScheduleSlots de $totalScheduleSlots actividades", modifier = Modifier.weight(1f))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard(title = "Presupuesto Restante", value = "$${String.format(Locale.US, "%.0f", remainingBalance)}", subtitle = "${spentPercentage.toInt()}% consumido", modifier = Modifier.weight(1f))
                        MetricCard(title = "Tareas Críticas", value = "$highPriorityPending", subtitle = "Alta prioridad pendientes", modifier = Modifier.weight(1f))
                    }

                    // Combined Quick Progress Rings
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Balance General de Organización", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))

                            PriorityProgressRow(title = "Avance en Tareas", count = completedTasks, total = totalTasks, color = primaryColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            PriorityProgressRow(title = "Cumplimiento de Rutinas / Horario", count = completedScheduleSlots, total = totalScheduleSlots, color = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.height(8.dp))
                            PriorityProgressRow(title = "Presupuesto Utilizado", count = totalExpenses.toInt(), total = totalBudgetAvailable.toInt(), color = Color(0xFFFB8C00))
                        }
                    }
                }

                1 -> {
                    // Tareas
                    Text("Estadísticas de Tareas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryColor)

                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Progreso Global de Cumplimiento", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                            Spacer(modifier = Modifier.height(16.dp))

                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeWidth = 18.dp.toPx()
                                    val diameter = size.minDimension - strokeWidth
                                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                                    drawArc(color = trackColor, startAngle = 0f, sweepAngle = 360f, useCenter = false, topLeft = topLeft, size = Size(diameter, diameter), style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                                    val sweepAngle = (taskCompletionPercentage / 100f) * 360f
                                    drawArc(color = primaryColor, startAngle = -90f, sweepAngle = sweepAngle, useCenter = false, topLeft = topLeft, size = Size(diameter, diameter), style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${taskCompletionPercentage.toInt()}%", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = primaryColor)
                                    Text("$completedTasks de $totalTasks completadas", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    // Priority Breakdown Card
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text("Desglose por Niveles de Prioridad", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))

                            val highCount = allTasks.count { it.priority == Priority.HIGH }
                            val mediumCount = allTasks.count { it.priority == Priority.MEDIUM }
                            val lowCount = allTasks.count { it.priority == Priority.LOW }

                            PriorityProgressRow(title = "Alta Prioridad", count = highCount, total = totalTasks, color = Color(0xFFE53935))
                            PriorityProgressRow(title = "Prioridad Media", count = mediumCount, total = totalTasks, color = Color(0xFFFB8C00))
                            PriorityProgressRow(title = "Baja Prioridad", count = lowCount, total = totalTasks, color = Color(0xFF4CAF50))
                        }
                    }
                }

                2 -> {
                    // Finanzas
                    Text("Análisis Financiero y Presupuesto", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryColor)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard(title = "Ingresos / Presupuesto", value = "$${String.format(Locale.US, "%.0f", totalBudgetAvailable)}", subtitle = "Monto mensual total", modifier = Modifier.weight(1f))
                        MetricCard(title = "Gastos Totales", value = "-$${String.format(Locale.US, "%.0f", totalExpenses)}", subtitle = "${spentPercentage.toInt()}% ejecutado", modifier = Modifier.weight(1f))
                    }

                    // Expenses Bar Breakdown Chart
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text("Desglose de Gastos por Categoría", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))

                            val maxExpense = expensesByCategory.values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

                            expensesByCategory.forEach { (cat, amount) ->
                                val fraction = (amount / maxExpense).toFloat()

                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(cat, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Text("$${String.format(Locale.US, "%.2f", amount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Canvas(modifier = Modifier.fillMaxWidth().height(10.dp)) {
                                        drawRoundRect(color = trackColor, size = Size(size.width, size.height), cornerRadius = CornerRadius(5.dp.toPx()))
                                        if (fraction > 0f) {
                                            drawRoundRect(color = Color(0xFFE53935), size = Size(size.width * fraction, size.height), cornerRadius = CornerRadius(5.dp.toPx()))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // Rutinas & Horario
                    Text("Análisis de Rutinas y Cumplimiento de Horario", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryColor)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard(title = "Actividades Totales", value = "$totalScheduleSlots", subtitle = "Programadas en el horario", modifier = Modifier.weight(1f))
                        MetricCard(title = "Cumplimiento", value = "${scheduleCompliancePercentage.toInt()}%", subtitle = "$completedScheduleSlots completadas", modifier = Modifier.weight(1f))
                    }

                    // Schedule Ring Chart Card
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Tasa de Cumplimiento de Rutina", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                            Spacer(modifier = Modifier.height(16.dp))

                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeWidth = 18.dp.toPx()
                                    val diameter = size.minDimension - strokeWidth
                                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                                    drawArc(color = trackColor, startAngle = 0f, sweepAngle = 360f, useCenter = false, topLeft = topLeft, size = Size(diameter, diameter), style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                                    val sweepAngle = (scheduleCompliancePercentage / 100f) * 360f
                                    drawArc(color = Color(0xFF4CAF50), startAngle = -90f, sweepAngle = sweepAngle, useCenter = false, topLeft = topLeft, size = Size(diameter, diameter), style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${scheduleCompliancePercentage.toInt()}%", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
                                    Text("$completedScheduleSlots de $totalScheduleSlots realizadas", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text("Distribución de Tiempo por Área", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))

                            val maxSlots = slotsByCategory.values.maxOrNull()?.coerceAtLeast(1) ?: 1

                            slotsByCategory.forEach { (cat, count) ->
                                val fraction = count.toFloat() / maxSlots

                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(cat, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Text("$count bloques", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Canvas(modifier = Modifier.fillMaxWidth().height(10.dp)) {
                                        drawRoundRect(color = trackColor, size = Size(size.width, size.height), cornerRadius = CornerRadius(5.dp.toPx()))
                                        if (fraction > 0f) {
                                            drawRoundRect(color = primaryColor, size = Size(size.width * fraction, size.height), cornerRadius = CornerRadius(5.dp.toPx()))
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
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PriorityProgressRow(
    title: String,
    count: Int,
    total: Int,
    color: Color
) {
    val fraction = if (total > 0) (count.toFloat() / total).coerceIn(0f, 1f) else 0f
    val percentage = (fraction * 100).toInt()

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text("$count ($percentage%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
    }
}
