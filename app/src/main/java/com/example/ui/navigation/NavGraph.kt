package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ui.screens.AboutDeveloperScreen
import com.example.ui.screens.AddEditTaskScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TaskDetailScreen
import com.example.ui.screens.ProgressDashboardScreen
import com.example.ui.screens.SuggestedRoutinesScreen
import com.example.ui.viewmodel.TaskViewModel
import com.example.ui.viewmodel.ProgressViewModel

object NavRoutes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val ADD_EDIT = "add_edit?taskId={taskId}"
    const val DETAIL = "detail/{taskId}"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val PROGRESS = "progress"
    const val SUGGESTIONS = "suggestions"

    fun addEditRoute(taskId: Int? = null): String {
        return if (taskId != null) "add_edit?taskId=$taskId" else "add_edit"
    }

    fun detailRoute(taskId: Int): String {
        return "detail/$taskId"
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: TaskViewModel,
    progressViewModel: ProgressViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH,
        modifier = modifier
    ) {
        // 1. Splash Screen
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // 2. Home Screen
        composable(NavRoutes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                progressViewModel = progressViewModel,
                onNavigateToAddEdit = { taskId ->
                    navController.navigate(NavRoutes.addEditRoute(taskId))
                },
                onNavigateToDetail = { taskId ->
                    navController.navigate(NavRoutes.detailRoute(taskId))
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.SETTINGS)
                },
                onNavigateToAbout = {
                    navController.navigate(NavRoutes.ABOUT)
                },
                onNavigateToProgress = {
                    navController.navigate(NavRoutes.PROGRESS)
                },
                onNavigateToSuggestions = {
                    navController.navigate(NavRoutes.SUGGESTIONS)
                }
            )
        }

        // 3. Add / Edit Screen
        composable(
            route = NavRoutes.ADD_EDIT,
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val taskIdRaw = backStackEntry.arguments?.getInt("taskId") ?: -1
            val taskId = if (taskIdRaw == -1) null else taskIdRaw
            AddEditTaskScreen(
                viewModel = viewModel,
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

         // 4. Detail Screen
        composable(
            route = NavRoutes.DETAIL,
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: -1
            TaskDetailScreen(
                viewModel = viewModel,
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    // Pop standard detail and navigate to edit
                    navController.navigate(NavRoutes.addEditRoute(id))
                }
            )
        }

        // 5. Settings Screen
        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSuggestions = {
                    navController.navigate(NavRoutes.SUGGESTIONS)
                }
            )
        }

        // 6. About Screen
        composable(NavRoutes.ABOUT) {
            AboutDeveloperScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 7. Progress Dashboard Screen
        composable(NavRoutes.PROGRESS) {
            ProgressDashboardScreen(
                viewModel = progressViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 8. Suggested Routines Screen
        composable(NavRoutes.SUGGESTIONS) {
            SuggestedRoutinesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(NavRoutes.detailRoute(taskId))
                }
            )
        }
    }
}
