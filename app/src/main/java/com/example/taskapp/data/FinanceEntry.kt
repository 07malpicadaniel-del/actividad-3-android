package com.example.taskapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType(val label: String) {
    INCOME("Ingreso"),
    EXPENSE("Gasto")
}

@Entity(tableName = "finance_entries")
data class FinanceEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String, // e.g., "Renta", "Despensa", "Servicios", "Transporte", "Ocio", "Otros"
    val dateMillis: Long
)
