package com.example.taskapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_slots")
data class ScheduleSlot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayOfWeek: Int, // 1 = Lunes, 2 = Martes, ..., 7 = Domingo
    val startTime: String, // e.g., "08:00"
    val endTime: String, // e.g., "09:30"
    val title: String,
    val category: String = "General",
    val colorHex: String = "#2196F3"
)
