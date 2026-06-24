package com.virginactive.android.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.virginactive.android.ui.detail.ClassDetailScreen
import com.virginactive.android.ui.home.HomeScreen
import com.virginactive.android.ui.login.LoginScreen
import com.virginactive.android.ui.timetable.TimetableScreen

@Composable
fun VaNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Login,
    ) {
        composable(Routes.Login) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(Routes.Shell) {
                        popUpTo(Routes.Login) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Routes.Shell) {
            AppScaffold(
                homeContent = { _, onOpenClassDetail ->
                    HomeScreen(onClassClick = onOpenClassDetail)
                },
                timetableContent = { snackbar, _ ->
                    TimetableScreen(snackbarHostState = snackbar)
                },
                onOpenClassDetail = { classId ->
                    navController.navigate(Routes.classDetail(classId))
                },
            )
        }

        composable(
            route = Routes.ClassDetail,
            arguments = listOf(navArgument(Routes.ClassDetailArg) { type = NavType.StringType }),
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getString(Routes.ClassDetailArg).orEmpty()
            ClassDetailScreen(
                classId = classId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
