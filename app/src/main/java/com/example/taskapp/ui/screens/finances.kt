package com.example.taskapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskapp.data.FinanceEntry
import com.example.taskapp.data.SavingGoal
import com.example.taskapp.data.TransactionType
import com.example.taskapp.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FinancesScreen(viewModel: TaskViewModel) {
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val allFinanceEntries by viewModel.allFinanceEntries.collectAsState()
    val allSavingGoals by viewModel.allSavingGoals.collectAsState()

    var selectedCategoryFilter by remember { mutableStateOf("Todas") }
    var showEntryDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showDepositDialogForGoal by remember { mutableStateOf<SavingGoal?>(null) }
    var entryToEdit by remember { mutableStateOf<FinanceEntry?>(null) }

    val totalExpenses = allFinanceEntries
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }

    val totalIncomeEntries = allFinanceEntries
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount }

    val totalBudgetAvailable = monthlyBudget + totalIncomeEntries
    val remainingBalance = totalBudgetAvailable - totalExpenses
    val spentPercentage = if (totalBudgetAvailable > 0) ((totalExpenses / totalBudgetAvailable) * 100).coerceAtMost(100.0) else 0.0

    val filteredEntries = remember(allFinanceEntries, selectedCategoryFilter) {
        if (selectedCategoryFilter == "Todas") {
            allFinanceEntries
        } else {
            allFinanceEntries.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    entryToEdit = null
                    showEntryDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Transacción")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Budget & Balance Summary Cards
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Presupuesto Mensual Base", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", totalBudgetAvailable)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(onClick = { showBudgetDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar Presupuesto")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Gastado", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "-$${String.format(Locale.US, "%.2f", totalExpenses)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Disponible", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", remainingBalance)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (remainingBalance >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress bar for spent budget
                    LinearProgressIndicator(
                        progress = { (spentPercentage / 100.0).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = if (spentPercentage > 90) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Gastado: ${spentPercentage.toInt()}% del presupuesto",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            // Savings Goals Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Savings, contentDescription = null, tint = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Metas de Ahorro", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                IconButton(onClick = { showGoalDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Meta de Ahorro")
                }
            }

            // Savings Goals Row
            if (allSavingGoals.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allSavingGoals, key = { it.id }) { goal ->
                        val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).coerceAtMost(1.0) else 0.0
                        Card(
                            modifier = Modifier
                                .width(220.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(goal.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { viewModel.deleteSavingGoal(goal) }, modifier = Modifier.size(20.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar Meta", tint = MaterialTheme.colorScheme.error)
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "$${String.format(Locale.US, "%.0f", goal.currentAmount)} / $${String.format(Locale.US, "%.0f", goal.targetAmount)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { progress.toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp),
                                    color = Color(0xFF2E7D32),
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Button(
                                    onClick = { showDepositDialogForGoal = goal },
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 2.dp)
                                ) {
                                    Text("+ Abonar Ahorro", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Category Filter Row
            val categories = listOf("Todas", "Renta", "Despensa", "Servicios", "Transporte", "Ocio", "Ahorro", "Otros")
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategoryFilter == category,
                        onClick = { selectedCategoryFilter = category },
                        label = { Text(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Entries List
            Box(modifier = Modifier.weight(1f)) {
                if (filteredEntries.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.MonetizationOn,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sin movimientos registrados.\nPresiona + para agregar un gasto o ingreso.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredEntries, key = { it.id }) { entry ->
                            FinanceEntryCard(
                                entry = entry,
                                onEdit = {
                                    entryToEdit = entry
                                    showEntryDialog = true
                                },
                                onDelete = { viewModel.deleteFinanceEntry(entry) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBudgetDialog) {
        BudgetDialog(
            currentBudget = monthlyBudget,
            onDismiss = { showBudgetDialog = false },
            onConfirm = { newBudget ->
                viewModel.setMonthlyBudget(newBudget)
                showBudgetDialog = false
            }
        )
    }

    if (showGoalDialog) {
        AddSavingGoalDialog(
            onDismiss = { showGoalDialog = false },
            onConfirm = { title, targetAmount ->
                viewModel.addSavingGoal(title, targetAmount)
                showGoalDialog = false
            }
        )
    }

    if (showDepositDialogForGoal != null) {
        DepositGoalDialog(
            goal = showDepositDialogForGoal!!,
            onDismiss = { showDepositDialogForGoal = null },
            onConfirm = { depositAmount ->
                viewModel.depositToSavingGoal(showDepositDialogForGoal!!, depositAmount)
                showDepositDialogForGoal = null
            }
        )
    }

    if (showEntryDialog) {
        FinanceEntryDialog(
            entry = entryToEdit,
            onDismiss = { showEntryDialog = false },
            onConfirm = { title, amount, type, category ->
                if (entryToEdit == null) {
                    viewModel.addFinanceEntry(title, amount, type, category)
                } else {
                    viewModel.updateFinanceEntry(
                        entryToEdit!!.copy(
                            title = title,
                            amount = amount,
                            type = type,
                            category = category
                        )
                    )
                }
                showEntryDialog = false
            }
        )
    }
}

@Composable
fun AddSavingGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, targetAmount: Double) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Meta de Ahorro") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre de la Meta (ej. Laptop, Viaje, Fondo)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Monto Objetivo ($)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = targetText.toDoubleOrNull()
                    if (title.isNotBlank() && amount != null && amount > 0) {
                        onConfirm(title, amount)
                    }
                },
                enabled = title.isNotBlank() && targetText.toDoubleOrNull() != null
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

@Composable
fun DepositGoalDialog(
    goal: SavingGoal,
    onDismiss: () -> Unit,
    onConfirm: (depositAmount: Double) -> Unit
) {
    var depositText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Abonar a Ahorro: ${goal.title}") },
        text = {
            OutlinedTextField(
                value = depositText,
                onValueChange = { depositText = it },
                label = { Text("Monto a Abonar ($)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = depositText.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        onConfirm(amount)
                    }
                },
                enabled = depositText.toDoubleOrNull() != null
            ) {
                Text("Abonar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun FinanceEntryCard(
    entry: FinanceEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val formattedDate = dateFormat.format(Date(entry.dateMillis))

    val isExpense = entry.type == TransactionType.EXPENSE
    val amountColor = if (isExpense) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
    val amountPrefix = if (isExpense) "-" else "+"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = entry.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "$amountPrefix$${String.format(Locale.US, "%.2f", entry.amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = amountColor
            )

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar Transacción")
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar Transacción",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun BudgetDialog(
    currentBudget: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var budgetText by remember { mutableStateOf(currentBudget.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Presupuesto Mensual") },
        text = {
            OutlinedTextField(
                value = budgetText,
                onValueChange = { budgetText = it },
                label = { Text("Monto ($)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = budgetText.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        onConfirm(amount)
                    }
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceEntryDialog(
    entry: FinanceEntry? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, type: TransactionType, category: String) -> Unit
) {
    var title by remember { mutableStateOf(entry?.title ?: "") }
    var amountText by remember { mutableStateOf(entry?.amount?.toString() ?: "") }
    var selectedType by remember { mutableStateOf(entry?.type ?: TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf(entry?.category ?: "Renta") }

    val categories = listOf("Renta", "Despensa", "Servicios", "Transporte", "Ocio", "Ahorro", "Otros")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (entry == null) "Nuevo Movimiento" else "Editar Movimiento") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Descripción (ej. Renta, Despensa)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Monto ($)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Tipo de Movimiento:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.label) }
                        )
                    }
                }

                Text("Categoría:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = amountText.toDoubleOrNull()
                    if (title.isNotBlank() && parsedAmount != null && parsedAmount > 0) {
                        onConfirm(title, parsedAmount, selectedType, selectedCategory)
                    }
                },
                enabled = title.isNotBlank() && amountText.toDoubleOrNull() != null
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
