package com.example.taskapp.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val scheduleDao: ScheduleDao,
    private val financeDao: FinanceDao,
    private val habitDao: HabitDao,
    private val dailyNoteDao: DailyNoteDao,
    private val savingGoalDao: SavingGoalDao
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

    // Finance operations
    val allFinanceEntries: Flow<List<FinanceEntry>> = financeDao.getAllFinanceEntries()

    suspend fun insertFinanceEntry(entry: FinanceEntry) = financeDao.insertEntry(entry)

    suspend fun updateFinanceEntry(entry: FinanceEntry) = financeDao.updateEntry(entry)

    suspend fun deleteFinanceEntry(entry: FinanceEntry) = financeDao.deleteEntry(entry)

    // Habit operations
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val allHabitLogs: Flow<List<HabitLog>> = habitDao.getAllHabitLogs()

    suspend fun insertHabit(habit: Habit) = habitDao.insertHabit(habit)
    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)
    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)
    suspend fun insertHabitLog(log: HabitLog) = habitDao.insertHabitLog(log)
    suspend fun deleteHabitLog(habitId: Long, dateMillis: Long) = habitDao.deleteHabitLog(habitId, dateMillis)

    // Daily Note operations
    val allDailyNotes: Flow<List<DailyNote>> = dailyNoteDao.getAllNotes()
    fun getNoteForDate(dateMillis: Long): Flow<DailyNote?> = dailyNoteDao.getNoteForDate(dateMillis)
    suspend fun insertDailyNote(note: DailyNote) = dailyNoteDao.insertNote(note)

    // Saving Goal operations
    val allSavingGoals: Flow<List<SavingGoal>> = savingGoalDao.getAllSavingGoals()
    suspend fun insertSavingGoal(goal: SavingGoal) = savingGoalDao.insertGoal(goal)
    suspend fun updateSavingGoal(goal: SavingGoal) = savingGoalDao.updateGoal(goal)
    suspend fun deleteSavingGoal(goal: SavingGoal) = savingGoalDao.deleteGoal(goal)
}
