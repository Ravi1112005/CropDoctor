package com.example.cropdoctor.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cropdoctor.ui.screens.diagnosis.DiagnosisScreen
import com.example.cropdoctor.ui.screens.diagnosis.DiagnosisViewModel
import com.example.cropdoctor.ui.screens.results.ResultScreen

sealed class Screen(val route: String) {
    object Diagnosis : Screen("diagnosis")
    object Result : Screen("result/{imageUri}") {
        fun createRoute(uri: Uri) = "result/${Uri.encode(uri.toString())}"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    val diagnosisViewModel: DiagnosisViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Diagnosis.route
    ) {
        composable(Screen.Diagnosis.route) {
            DiagnosisScreen(
                viewModel = diagnosisViewModel,
                onNavigateToResult = { uri ->
                    navController.navigate(Screen.Result.createRoute(uri))
                }
            )
        }

        composable(
            route = Screen.Result.route,
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val uriString = backStackEntry.arguments?.getString("imageUri")

            // Safely parse the URI, remembering the result. Returns null on failure.
            val imageUri = remember(uriString) {
                try {
                    uriString?.let { Uri.parse(Uri.decode(it)) }
                } catch (e: Exception) {
                    // Invalid URI format
                    null
                }
            }

            val results = imageUri?.let { diagnosisViewModel.getResultForUri(it) }

            // This single LaunchedEffect handles all failure cases: invalid URI or missing results.
            // It is correctly placed at the top level of the composable.
            LaunchedEffect(imageUri, results) {
                if (imageUri == null || results.isNullOrEmpty()) {
                    navController.popBackStack()
                }
            }

            // Only render the screen if we have valid data. This prevents flicker or crashes
            // while the LaunchedEffect above prepares to navigate back.
            if (results != null && results.isNotEmpty()) {
                ResultScreen(
                    result = results.first(),
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
