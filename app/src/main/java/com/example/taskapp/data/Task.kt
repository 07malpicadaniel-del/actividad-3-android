package com.example.taskapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Priority(val label: String) {
    LOW("Baja"),
    MEDIUM("Media"),
    HIGH("Alta")
}

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val dateMillis: Long,
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val category: String = "General"
)
