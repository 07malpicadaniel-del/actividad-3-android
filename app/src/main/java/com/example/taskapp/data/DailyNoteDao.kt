package com.example.taskapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyNoteDao {
    @Query("SELECT * FROM daily_notes WHERE dateMillis = :dateMillis LIMIT 1")
    fun getNoteForDate(dateMillis: Long): Flow<DailyNote?>

    @Query("SELECT * FROM daily_notes")
    fun getAllNotes(): Flow<List<DailyNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: DailyNote)
}
