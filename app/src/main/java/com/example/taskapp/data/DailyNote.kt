package com.example.taskapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_notes")
data class DailyNote(
    @PrimaryKey
    val dateMillis: Long, // Start of day timestamp
    val moodEmoji: String = "Excelente", // e.g., "Excelente", "Bueno", "Normal", "Cansado"
    val noteText: String = ""
)
