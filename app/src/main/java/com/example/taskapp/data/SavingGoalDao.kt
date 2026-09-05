package com.example.taskapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingGoalDao {
    @Query("SELECT * FROM saving_goals ORDER BY id ASC")
    fun getAllSavingGoals(): Flow<List<SavingGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingGoal)

    @Update
    suspend fun updateGoal(goal: SavingGoal)

    @Delete
    suspend fun deleteGoal(goal: SavingGoal)
}
