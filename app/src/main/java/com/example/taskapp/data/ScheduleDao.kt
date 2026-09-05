package com.example.taskapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_slots ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllScheduleSlots(): Flow<List<ScheduleSlot>>

    @Query("SELECT * FROM schedule_slots WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getScheduleSlotsForDay(dayOfWeek: Int): Flow<List<ScheduleSlot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: ScheduleSlot)

    @Update
    suspend fun updateSlot(slot: ScheduleSlot)

    @Delete
    suspend fun deleteSlot(slot: ScheduleSlot)
}
