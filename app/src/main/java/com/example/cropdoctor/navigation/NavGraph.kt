package com.example.cropdoctor.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
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
    navController: NavHostController = rememberNavController(),
) {
    val diagnosisViewModel: DiagnosisViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Diagnosis.route
    ) {
        composable(Screen.Diagnosis.route) {
            // The DiagnosisScreen's only job is to capture an image and navigate.
            DiagnosisScreen(
                onImageCaptured = { uri ->
                    navController.navigate(Screen.Result.createRoute(uri))
                }
            )
        }

        composable(
            route = Screen.Result.route,
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val uriString = backStackEntry.arguments?.getString("imageUri")
            val imageUri = remember(uriString) {
                try {
                    uriString?.let { Uri.parse(Uri.decode(it)) }
                } catch (e: Exception) {
                    null // Invalid URI format, will be handled in ResultScreen
                }
            }

            if (imageUri != null) {
                ResultScreen(
                    imageUri = imageUri,
                    viewModel = diagnosisViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
