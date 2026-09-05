package com.example.taskapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskapp.data.*
import com.example.taskapp.ui.theme.ThemePreset
import com.example.taskapp.util.NotificationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class FilterStatus(val label: String) {
    ALL("Todas"),
    PENDING("Pendientes"),
    COMPLETED("Completadas")
}

enum class RoutineType(val title: String, val description: String) {
    OFICINA("Rutina de Oficina / Trabajo", "Enfocada en trabajo productivo, pausas para comer y ejercicio."),
    ESTUDIANTE("Rutina de Estudiante", "Bloques de clases, estudio en profundidad y tiempo libre."),
    FIN_DE_SEMANA("Rutina de Fin de Semana", "Tiempo para hobbies, familia, deporte y descanso.")
}

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    val isDarkTheme = MutableStateFlow(false)
    val themePreset = MutableStateFlow(ThemePreset.MONOCHROME)

    val selectedDateMillis = MutableStateFlow(getStartOfDay(System.currentTimeMillis()))

    val allTasks: StateFlow<List<Task>>

    // Filtering states
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("Todas")
    val selectedFilterStatus = MutableStateFlow(FilterStatus.ALL)

    // Flow of tasks filtered by search, category, and completion status
    val filteredTasks: StateFlow<List<Task>>

    // Flow of tasks grouped by start of day (timestamp) for quick calendar indicators
    val tasksGroupedByDate: StateFlow<Map<Long, List<Task>>>

    val tasksForSelectedDate: StateFlow<List<Task>>

    // Schedule / Horario Personal States
    val selectedScheduleDay = MutableStateFlow(1) // 1 = Lunes, ..., 7 = Domingo
    val scheduleSlotsForSelectedDay: StateFlow<List<ScheduleSlot>>
    val allScheduleSlots: StateFlow<List<ScheduleSlot>>

    // Finance States
    val monthlyBudget = MutableStateFlow(1000.0) // Monthly income/budget limit (e.g. $1000)
    val allFinanceEntries: StateFlow<List<FinanceEntry>>
    val allSavingGoals: StateFlow<List<SavingGoal>>

    // Habit States
    val allHabits: StateFlow<List<Habit>>
    val allHabitLogs: StateFlow<List<HabitLog>>

    // Daily Note State
    val currentDailyNote: StateFlow<DailyNote?>

    init {
        val database = TaskDatabase.getDatabase(application)
        repository = TaskRepository(
            database.taskDao(),
            database.scheduleDao(),
            database.financeDao(),
            database.habitDao(),
            database.dailyNoteDao(),
            database.savingGoalDao()
        )

        allTasks = repository.allTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        filteredTasks = combine(
            allTasks,
            searchQuery,
            selectedCategory,
            selectedFilterStatus
        ) { tasks, query, category, status ->
            tasks.filter { task ->
                val matchesQuery = query.isBlank() ||
                        task.title.contains(query, ignoreCase = true) ||
                        task.description.contains(query, ignoreCase = true)

                val matchesCategory = (category == "Todas" || task.category.equals(category, ignoreCase = true))

                val matchesStatus = when (status) {
                    FilterStatus.ALL -> true
                    FilterStatus.PENDING -> !task.isCompleted
                    FilterStatus.COMPLETED -> task.isCompleted
                }

                matchesQuery && matchesCategory && matchesStatus
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        tasksGroupedByDate = allTasks.map { tasks ->
            tasks.groupBy { getStartOfDay(it.dateMillis) }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

        tasksForSelectedDate = selectedDateMillis.flatMapLatest { dateMillis ->
            val startOfDay = getStartOfDay(dateMillis)
            val endOfDay = getEndOfDay(dateMillis)
            repository.getTasksByDateRange(startOfDay, endOfDay)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allScheduleSlots = repository.allScheduleSlots.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        scheduleSlotsForSelectedDay = selectedScheduleDay.flatMapLatest { dayOfWeek ->
            repository.getScheduleSlotsForDay(dayOfWeek)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allFinanceEntries = repository.allFinanceEntries.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allSavingGoals = repository.allSavingGoals.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allHabits = repository.allHabits.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allHabitLogs = repository.allHabitLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        currentDailyNote = selectedDateMillis.flatMapLatest { millis ->
            repository.getNoteForDate(getStartOfDay(millis))
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    fun toggleTheme() {
        isDarkTheme.value = !isDarkTheme.value
    }

    fun setThemePreset(preset: ThemePreset) {
        themePreset.value = preset
    }

    fun selectDate(millis: Long) {
        selectedDateMillis.value = getStartOfDay(millis)
    }

    fun addTask(
        title: String,
        description: String,
        dateMillis: Long,
        priority: Priority = Priority.MEDIUM,
        category: String = "General"
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val newTask = Task(
                title = title.trim(),
                description = description.trim(),
                dateMillis = dateMillis,
                isCompleted = false,
                priority = priority,
                category = category.trim().ifBlank { "General" }
            )
            repository.insertTask(newTask)

            // Send notification for high priority task
            if (priority == Priority.HIGH) {
                NotificationHelper.sendNotification(
                    getApplication(),
                    "Tarea de Alta Prioridad Creada",
                    "Recordatorio: ${newTask.title}"
                )
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun deleteCompletedTasks() {
        viewModelScope.launch {
            repository.deleteCompletedTasks()
        }
    }

    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    // Schedule Slot Methods
    fun selectScheduleDay(dayOfWeek: Int) {
        selectedScheduleDay.value = dayOfWeek
    }

    fun addScheduleSlot(
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        title: String,
        category: String = "General",
        colorHex: String = "#2196F3"
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val slot = ScheduleSlot(
                dayOfWeek = dayOfWeek,
                startTime = startTime,
                endTime = endTime,
                title = title.trim(),
                category = category.trim().ifBlank { "General" },
                colorHex = colorHex
            )
            repository.insertScheduleSlot(slot)
        }
    }

    fun updateScheduleSlot(slot: ScheduleSlot) {
        viewModelScope.launch {
            repository.updateScheduleSlot(slot)
        }
    }

    fun deleteScheduleSlot(slot: ScheduleSlot) {
        viewModelScope.launch {
            repository.deleteScheduleSlot(slot)
        }
    }

    fun toggleScheduleSlotCompleted(slot: ScheduleSlot) {
        viewModelScope.launch {
            repository.updateScheduleSlot(slot.copy(isCompleted = !slot.isCompleted))
        }
    }

    // Predefined Routines Generator
    fun applyPredefinedRoutine(dayOfWeek: Int, routineType: RoutineType) {
        viewModelScope.launch {
            val presetSlots = when (routineType) {
                RoutineType.OFICINA -> listOf(
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "07:00", endTime = "08:00", title = "Ejercicio y Desayuno", category = "Ejercicio", colorHex = "#4CAF50"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "08:30", endTime = "13:00", title = "Trabajo Enfoque", category = "Trabajo", colorHex = "#2196F3"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "13:00", endTime = "14:00", title = "Almuerzo y Descanso", category = "Almuerzo", colorHex = "#FF9800"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "14:00", endTime = "18:00", title = "Trabajo y Reuniones", category = "Trabajo", colorHex = "#2196F3"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "18:30", endTime = "20:00", title = "Gimnasio / Deporte", category = "Ejercicio", colorHex = "#4CAF50"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "20:30", endTime = "22:30", title = "Tiempo Libre y Cena", category = "Descanso", colorHex = "#9C27B0")
                )
                RoutineType.ESTUDIANTE -> listOf(
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "07:30", endTime = "08:30", title = "Preparación y Café", category = "General", colorHex = "#4CAF50"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "08:30", endTime = "12:30", title = "Clases / Estudio", category = "Estudios", colorHex = "#00BCD4"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "12:30", endTime = "13:30", title = "Almuerzo", category = "Almuerzo", colorHex = "#FF9800"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "14:00", endTime = "17:00", title = "Proyectos y Tareas", category = "Estudios", colorHex = "#00BCD4"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "17:30", endTime = "19:00", title = "Caminata / Deporte", category = "Ejercicio", colorHex = "#4CAF50"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "19:30", endTime = "22:00", title = "Lectura / Descanso", category = "Descanso", colorHex = "#9C27B0")
                )
                RoutineType.FIN_DE_SEMANA -> listOf(
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "09:00", endTime = "10:00", title = "Desayuno y Relax", category = "Descanso", colorHex = "#E91E63"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "10:30", endTime = "12:30", title = "Hobbies / Proyectos Personales", category = "Personal", colorHex = "#9C27B0"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "13:00", endTime = "15:00", title = "Comida en Familia", category = "General", colorHex = "#FF9800"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "15:30", endTime = "18:30", title = "Paseo / Actividad Libre", category = "Personal", colorHex = "#E91E63"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "19:00", endTime = "22:00", title = "Película / Ocio", category = "Descanso", colorHex = "#9C27B0")
                )
            }

            presetSlots.forEach { slot ->
                repository.insertScheduleSlot(slot)
            }
        }
    }

    // Finance Methods
    fun setMonthlyBudget(budget: Double) {
        if (budget > 0) {
            monthlyBudget.value = budget
        }
    }

    fun addFinanceEntry(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        dateMillis: Long = System.currentTimeMillis()
    ) {
        if (title.isBlank() || amount <= 0) return
        viewModelScope.launch {
            val entry = FinanceEntry(
                title = title.trim(),
                amount = amount,
                type = type,
                category = category.trim().ifBlank { "General" },
                dateMillis = dateMillis
            )
            repository.insertFinanceEntry(entry)
        }
    }

    fun updateFinanceEntry(entry: FinanceEntry) {
        viewModelScope.launch {
            repository.updateFinanceEntry(entry)
        }
    }

    fun deleteFinanceEntry(entry: FinanceEntry) {
        viewModelScope.launch {
            repository.deleteFinanceEntry(entry)
        }
    }

    // Habit Methods
    fun addHabit(title: String, category: String = "General", colorHex: String = "#4CAF50") {
        if (title.isBlank()) return
        viewModelScope.launch {
            val habit = Habit(title = title.trim(), category = category, colorHex = colorHex)
            repository.insertHabit(habit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    fun toggleHabitForDate(habitId: Long, dateMillis: Long, isCurrentlyCompleted: Boolean) {
        viewModelScope.launch {
            val dayStart = getStartOfDay(dateMillis)
            if (isCurrentlyCompleted) {
                repository.deleteHabitLog(habitId, dayStart)
            } else {
                repository.insertHabitLog(HabitLog(habitId = habitId, dateMillis = dayStart))
            }
        }
    }

    fun getHabitStreak(habitId: Long, logs: List<HabitLog>): Int {
        val habitLogsDates = logs
            .filter { it.habitId == habitId }
            .map { getStartOfDay(it.dateMillis) }
            .toSet()

        var streak = 0
        val cal = Calendar.getInstance().apply { timeInMillis = getStartOfDay(System.currentTimeMillis()) }

        while (true) {
            val dateToCheck = cal.timeInMillis
            if (habitLogsDates.contains(dateToCheck)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                // If today is not completed yet, check yesterday before stopping streak
                if (streak == 0) {
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    if (habitLogsDates.contains(cal.timeInMillis)) {
                        streak++
                        cal.add(Calendar.DAY_OF_YEAR, -1)
                        continue
                    }
                }
                break
            }
        }
        return streak
    }

    // Daily Note Methods
    fun saveDailyNote(moodEmoji: String, text: String) {
        viewModelScope.launch {
            val note = DailyNote(
                dateMillis = selectedDateMillis.value,
                moodEmoji = moodEmoji,
                noteText = text.trim()
            )
            repository.insertDailyNote(note)
        }
    }

    // Saving Goal Methods
    fun addSavingGoal(title: String, targetAmount: Double, colorHex: String = "#4CAF50") {
        if (title.isBlank() || targetAmount <= 0) return
        viewModelScope.launch {
            val goal = SavingGoal(title = title.trim(), targetAmount = targetAmount, colorHex = colorHex)
            repository.insertSavingGoal(goal)
        }
    }

    fun depositToSavingGoal(goal: SavingGoal, depositAmount: Double) {
        if (depositAmount <= 0) return
        viewModelScope.launch {
            val updatedGoal = goal.copy(currentAmount = goal.currentAmount + depositAmount)
            repository.updateSavingGoal(updatedGoal)

            // Also add a finance entry for the savings deposit
            val entry = FinanceEntry(
                title = "Ahorro: ${goal.title}",
                amount = depositAmount,
                type = TransactionType.EXPENSE,
                category = "Ahorro",
                dateMillis = System.currentTimeMillis()
            )
            repository.insertFinanceEntry(entry)
        }
    }

    fun deleteSavingGoal(goal: SavingGoal) {
        viewModelScope.launch {
            repository.deleteSavingGoal(goal)
        }
    }

    companion object {
        val categories = listOf("Todas", "General", "Trabajo", "Personal", "Estudios", "Hogar")
        val financeCategories = listOf("Renta", "Despensa", "Servicios", "Transporte", "Ocio", "Ahorro", "Otros")

        fun getStartOfDay(timeMillis: Long): Long {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = timeMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return calendar.timeInMillis
        }

        fun getEndOfDay(timeMillis: Long): Long {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = timeMillis
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            return calendar.timeInMillis
        }
    }
}

class TaskViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
