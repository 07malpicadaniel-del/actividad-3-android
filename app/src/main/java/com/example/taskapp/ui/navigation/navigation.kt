package com.example.taskapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.taskapp.ui.screens.CalendarScreen
import com.example.taskapp.ui.screens.ScheduleScreen
import com.example.taskapp.ui.screens.StatsScreen
import com.example.taskapp.ui.screens.TasksScreen
import com.example.taskapp.ui.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppNavigation(viewModel: TaskViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "tasks"

    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    val topBarTitle = when (currentRoute) {
        "tasks" -> "Agenda - Mis Tareas"
        "calendar" -> "Agenda - Calendario"
        "stats" -> "Agenda - Estadísticas"
        "schedule" -> "Agenda - Horario Personal"
        else -> "Agenda"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topBarTitle) },
                actions = {
                    IconButton(onClick = { viewModel.toggleTheme() }) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Cambiar modo Claro/Oscuro"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Tareas") },
                    label = { Text("Tareas") },
                    selected = currentRoute == "tasks",
                    onClick = {
                        navController.navigate("tasks") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Calendario") },
                    label = { Text("Calendario") },
                    selected = currentRoute == "calendar",
                    onClick = {
                        navController.navigate("calendar") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Gráficas") },
                    label = { Text("Gráficas") },
                    selected = currentRoute == "stats",
                    onClick = {
                        navController.navigate("stats") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Schedule, contentDescription = "Horario") },
                    label = { Text("Horario") },
                    selected = currentRoute == "schedule",
                    onClick = {
                        navController.navigate("schedule") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "tasks",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("tasks") { TasksScreen(viewModel = viewModel) }
            composable("calendar") { CalendarScreen(viewModel = viewModel) }
            composable("stats") { StatsScreen(viewModel = viewModel) }
            composable("schedule") { ScheduleScreen(viewModel = viewModel) }
        }
    }
}
