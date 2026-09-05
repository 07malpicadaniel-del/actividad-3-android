package com.example.taskapp.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val scheduleDao: ScheduleDao
) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    fun getTasksByDateRange(startOfDay: Long, endOfDay: Long): Flow<List<Task>> {
        return taskDao.getTasksByDateRange(startOfDay, endOfDay)
    }

    suspend fun insertTask(task: Task) = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun deleteCompletedTasks() = taskDao.deleteCompletedTasks()

    // Schedule Slot operations
    val allScheduleSlots: Flow<List<ScheduleSlot>> = scheduleDao.getAllScheduleSlots()

    fun getScheduleSlotsForDay(dayOfWeek: Int): Flow<List<ScheduleSlot>> {
        return scheduleDao.getScheduleSlotsForDay(dayOfWeek)
    }

    suspend fun insertScheduleSlot(slot: ScheduleSlot) = scheduleDao.insertSlot(slot)

    suspend fun updateScheduleSlot(slot: ScheduleSlot) = scheduleDao.updateSlot(slot)

    suspend fun deleteScheduleSlot(slot: ScheduleSlot) = scheduleDao.deleteSlot(slot)
}
