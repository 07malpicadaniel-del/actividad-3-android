package com.example.taskapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    @Query("SELECT * FROM finance_entries ORDER BY dateMillis DESC")
    fun getAllFinanceEntries(): Flow<List<FinanceEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: FinanceEntry)

    @Update
    suspend fun updateEntry(entry: FinanceEntry)

    @Delete
    suspend fun deleteEntry(entry: FinanceEntry)
}
