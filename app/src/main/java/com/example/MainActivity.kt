package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.data.WeatherRepository
import com.example.notification.WeatherNotificationManager
import com.example.ui.WeatherDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {

    private lateinit var notificationManager: WeatherNotificationManager
    private lateinit var weatherRepository: WeatherRepository

    private val weatherViewModel: WeatherViewModel by viewModels {
        WeatherViewModel.Factory(weatherRepository, notificationManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inyección manual de dependencias
        notificationManager = WeatherNotificationManager(applicationContext)
        weatherRepository = WeatherRepository(applicationContext, notificationManager)

        setContent {
            val isDarkMode by weatherViewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                WeatherDashboardScreen(viewModel = weatherViewModel)
            }
        }
    }
}
