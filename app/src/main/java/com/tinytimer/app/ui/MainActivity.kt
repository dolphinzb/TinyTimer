package com.tinytimer.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tinytimer.app.ui.pages.GroupPage
import com.tinytimer.app.ui.pages.HistoryPage
import com.tinytimer.app.ui.pages.TimerPage
import com.tinytimer.app.ui.theme.TinyTimerTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        setContent {
            TinyTimerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TinyTimerApp()
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun TinyTimerApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "timer"
    ) {
        composable("timer") {
            TimerPage(
                onNavigateToGroups = { navController.navigate("groups") },
                onNavigateToHistory = { navController.navigate("history") }
            )
        }
        composable("groups") {
            GroupPage(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("history") {
            HistoryPage(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
