package com.example.taskapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskapp.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class FilterStatus(val label: String) {
    ALL("Todas"),
    PENDING("Pendientes"),
    COMPLETED("Completadas")
}

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    val isDarkTheme = MutableStateFlow(false)

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

    init {
        val database = TaskDatabase.getDatabase(application)
        repository = TaskRepository(database.taskDao(), database.scheduleDao())

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

        scheduleSlotsForSelectedDay = selectedScheduleDay.flatMapLatest { dayOfWeek ->
            repository.getScheduleSlotsForDay(dayOfWeek)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun toggleTheme() {
        isDarkTheme.value = !isDarkTheme.value
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

    companion object {
        val categories = listOf("Todas", "General", "Trabajo", "Personal", "Estudios", "Hogar")

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
