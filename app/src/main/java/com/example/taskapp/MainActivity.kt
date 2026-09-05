package com.example.taskapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.taskapp.ui.navigation.MainAppNavigation
import com.example.taskapp.ui.theme.TaskAppTheme
import com.example.taskapp.ui.viewmodel.TaskViewModel
import com.example.taskapp.ui.viewmodel.TaskViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            TaskAppTheme(darkTheme = isDarkTheme) {
                MainAppNavigation(viewModel = viewModel)
            }
        }
    }
}
