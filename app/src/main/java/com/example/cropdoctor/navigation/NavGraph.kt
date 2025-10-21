package com.example.cropdoctor.navigation

import android.net.Uri
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cropdoctor.ui.components.DrawerContent
import com.example.cropdoctor.ui.screens.auth.LoginScreen
import com.example.cropdoctor.ui.screens.auth.RegisterScreen
import com.example.cropdoctor.ui.screens.dashboard.HomeScreen
import com.example.cropdoctor.ui.screens.dashboard.ProfileScreen
import com.example.cropdoctor.ui.screens.dashboard.ProfileViewModel
import com.example.cropdoctor.ui.screens.diagnosis.DiagnosisScreen
import com.example.cropdoctor.ui.screens.diagnosis.DiagnosisViewModel
import com.example.cropdoctor.ui.screens.history.HistoryResultViewModel
import com.example.cropdoctor.ui.screens.history.HistoryScreen
import com.example.cropdoctor.ui.screens.history.HistoryViewModel
import com.example.cropdoctor.ui.screens.results.ResultScreen
import com.example.cropdoctor.ui.screens.settings.SettingsScreen
import com.example.cropdoctor.ui.screens.settings.SettingsViewModel
import com.example.cropdoctor.ui.screens.settings.Theme
import com.example.cropdoctor.ui.theme.CropDoctorTheme
import kotlinx.coroutines.launch

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    object Login : Screen("login", "Login")
    object Register : Screen("register", "Register")
    object Diagnosis : Screen("diagnosis", "Diagnosis")
    object Result : Screen("result/{imageUri}", "Result") {
        fun createRoute(uri: Uri) = "result/${Uri.encode(uri.toString())}"
    }
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Apps)
    object Profile : Screen("profile", "My Account")
    object ScanCrop : Screen("scan_crop", "Scan Crop", Icons.Filled.DocumentScanner)
    object History : Screen("history", "My Diagnosis History", Icons.Filled.History)
    object HistoryResult : Screen("history_result", "History Result")
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

val drawerItems = listOf(
    Screen.Dashboard,
    Screen.ScanCrop,
    Screen.History,
    Screen.Settings
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
) {
    val diagnosisViewModel: DiagnosisViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val historyViewModel: HistoryViewModel = viewModel()
    val historyResultViewModel: HistoryResultViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()
    val settingsUiState by settingsViewModel.uiState.collectAsState()

    val useDarkTheme = when (settingsUiState.theme) {
        Theme.LIGHT -> false
        Theme.DARK -> true
        Theme.SYSTEM -> isSystemInDarkTheme()
    }

    CropDoctorTheme(darkTheme = useDarkTheme) {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val isAuthScreen = currentRoute == Screen.Login.route || currentRoute == Screen.Register.route

        ModalNavigationDrawer(
            drawerContent = {
                val user by profileViewModel.user.collectAsState()
                DrawerContent(
                    user = user,
                    onItemClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(it.route)
                    }
                )
            },
            drawerState = drawerState,
            gesturesEnabled = !isAuthScreen
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Login.route
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(
                        onRegisterClick = { navController.navigate(Screen.Register.route) },
                        onLoginSuccess = { navController.navigate(Screen.Dashboard.route) { popUpTo(Screen.Login.route) { inclusive = true } } }
                    )
                }
                composable(Screen.Register.route) {
                    RegisterScreen(
                        onLoginClick = { navController.popBackStack() },
                        onRegisterSuccess = { navController.navigate(Screen.Dashboard.route) { popUpTo(Screen.Login.route) { inclusive = true } } }
                    )
                }
                composable(Screen.Dashboard.route) {
                    HomeScreen(
                        navController = navController,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        historyViewModel = historyViewModel,
                        historyResultViewModel = historyResultViewModel
                    )
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        navController = navController,
                        viewModel = profileViewModel
                    )
                }
                composable(Screen.ScanCrop.route) {
                    DiagnosisScreen(
                        onImageCaptured = { uri -> navController.navigate(Screen.Result.createRoute(uri)) }
                    )
                }
                composable(Screen.History.route) {
                    HistoryScreen(
                        navController = navController,
                        historyViewModel = historyViewModel,
                        historyResultViewModel = historyResultViewModel,
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                }
                composable(Screen.HistoryResult.route) {
                    ResultScreen(
                        historyResultViewModel = historyResultViewModel,
                        navController = navController,
                        onNavigateBack = { navController.popBackStack() },
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        viewModel = settingsViewModel
                    )
                }
                composable(
                    route = Screen.Result.route,
                    arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
                ) { backStackEntry ->
                    val uriString = backStackEntry.arguments?.getString("imageUri")
                    val imageUri = remember(uriString) {
                        try {
                            uriString?.let { Uri.decode(it).toUri() }
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (imageUri != null) {
                        ResultScreen(
                            imageUri = imageUri,
                            viewModel = diagnosisViewModel,
                            navController = navController,
                            onNavigateBack = { navController.popBackStack() },
                            onMenuClick = { scope.launch { drawerState.open() } }
                        )
                    }
                }
            }
        }
    }
}
