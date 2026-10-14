package org.zenith26.fest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.zenith26.fest.ui.admin.AdminDashboardScreen
import org.zenith26.fest.ui.admin.AdminViewModel
import org.zenith26.fest.ui.common.FestTopAppBar
import org.zenith26.fest.ui.settings.ServerConfigDialog
import org.zenith26.fest.ui.student.StudentPortalScreen
import org.zenith26.fest.ui.student.StudentViewModel
import org.zenith26.fest.ui.theme.Slate900
import org.zenith26.fest.ui.theme.ZenithFestTheme

class MainActivity : ComponentActivity() {

    private val studentViewModel: StudentViewModel by viewModels()
    private val adminViewModel: AdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZenithFestTheme {
                var currentScreen by remember { mutableStateOf("student") }
                var showServerConfig by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        FestTopAppBar(
                            currentScreen = currentScreen,
                            onNavigate = { currentScreen = it },
                            onOpenServerConfig = { showServerConfig = true },
                            onRefresh = {
                                if (currentScreen == "student") {
                                    studentViewModel.fetchEvents()
                                } else {
                                    adminViewModel.refreshAllData()
                                }
                            }
                        )
                    },
                    containerColor = Slate900
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Slate900)
                            .padding(innerPadding)
                    ) {
                        if (currentScreen == "student") {
                            StudentPortalScreen(viewModel = studentViewModel)
                        } else {
                            AdminDashboardScreen(viewModel = adminViewModel)
                        }

                        if (showServerConfig) {
                            ServerConfigDialog(
                                onDismiss = { showServerConfig = false },
                                onUrlUpdated = {
                                    studentViewModel.fetchEvents()
                                    adminViewModel.refreshAllData()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
