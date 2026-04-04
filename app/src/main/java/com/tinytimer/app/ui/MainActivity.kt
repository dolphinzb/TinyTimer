package com.tinytimer.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.map
import com.tinytimer.app.ui.pages.AboutPage
import com.tinytimer.app.ui.pages.GroupPage
import com.tinytimer.app.ui.pages.HistoryPage
import com.tinytimer.app.ui.pages.SettingsPage
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
    val currentRoute by navController.currentBackStackEntryFlow
        .map { it.destination.route }
        .collectAsState(initial = "timer")

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Timer, contentDescription = "计时") },
                    label = { Text("计时") },
                    selected = currentRoute == "timer",
                    onClick = { navController.navigate("timer") { launchSingleTop = true } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, contentDescription = "历史") },
                    label = { Text("历史") },
                    selected = currentRoute == "history",
                    onClick = { navController.navigate("history") { launchSingleTop = true } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "设置") },
                    label = { Text("设置") },
                    selected = currentRoute == "settings",
                    onClick = { navController.navigate("settings") { launchSingleTop = true } }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "timer",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("timer") {
                Column(modifier = Modifier.padding(paddingValues)) {
                    TimerPage()
                }
            }
            composable("history") {
                Column(modifier = Modifier.padding(paddingValues)) {
                    HistoryPage()
                }
            }
            composable("settings") {
                Column(modifier = Modifier.padding(paddingValues)) {
                    SettingsPage(
                        onNavigateToGroups = { navController.navigate("groups") },
                        onNavigateToAbout = { navController.navigate("about") }
                    )
                }
            }
            composable("groups") {
                Column(modifier = Modifier.padding(paddingValues)) {
                    GroupPage(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
            composable("about") {
                Column(modifier = Modifier.padding(paddingValues)) {
                    AboutPage(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}