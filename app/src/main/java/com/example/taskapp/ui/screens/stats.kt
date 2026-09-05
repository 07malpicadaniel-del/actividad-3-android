package com.example.taskapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.taskapp.ui.viewmodel.TaskViewModel

@Composable
fun StatsScreen(viewModel: TaskViewModel) {
    val allTasks by viewModel.allTasks.collectAsState()

    val totalTasks = allTasks.size
    val completedTasks = allTasks.count { it.isCompleted }
    val pendingTasks = totalTasks - completedTasks
    val completionPercentage = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks) * 100f else 0f

    val highPriorityPending = allTasks.count { !it.isCompleted && it.priority == Priority.HIGH }

    val categories = listOf("General", "Trabajo", "Personal", "Estudios", "Hogar")
    val tasksByCategory = categories.associateWith { cat ->
        allTasks.count { it.category.equals(cat, ignoreCase = true) }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Headline & Cards
        Text(
            text = "Resumen de Rendimiento",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Metrics 2x2 Grid Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Total Tareas",
                value = totalTasks.toString(),
                subtitle = "Registradas",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Completadas",
                value = completedTasks.toString(),
                subtitle = "${completionPercentage.toInt()}% del total",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Pendientes",
                value = pendingTasks.toString(),
                subtitle = "$highPriorityPending de alta prioridad",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Efectividad",
                value = "${completionPercentage.toInt()}%",
                subtitle = "Tasa de éxito",
                modifier = Modifier.weight(1f)
            )
        }

        // Circular Completion Ring Chart Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Progreso Global de Cumplimiento",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(160.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 18.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                        // Track Arc
                        drawArc(
                            color = trackColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Progress Arc
                        val sweepAngle = (completionPercentage / 100f) * 360f
                        drawArc(
                            color = primaryColor,
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${completionPercentage.toInt()}%",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$completedTasks de $totalTasks",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Category Bar Chart Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Distribución de Tareas por Categoría",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                val maxCategoryCount = tasksByCategory.values.maxOrNull()?.coerceAtLeast(1) ?: 1

                tasksByCategory.forEach { (category, count) ->
                    val fraction = count.toFloat() / maxCategoryCount

                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(category, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("$count tareas", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                        ) {
                            drawRoundRect(
                                color = trackColor,
                                size = Size(size.width, size.height),
                                cornerRadius = CornerRadius(6.dp.toPx())
                            )
                            if (fraction > 0f) {
                                drawRoundRect(
                                    color = primaryColor,
                                    size = Size(size.width * fraction, size.height),
                                    cornerRadius = CornerRadius(6.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }
        }

        // Priority Breakdown Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Desglose por Niveles de Prioridad",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

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
    val fraction = if (total > 0) count.toFloat() / total else 0f
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
