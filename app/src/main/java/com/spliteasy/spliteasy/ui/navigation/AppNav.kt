package com.spliteasy.spliteasy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spliteasy.spliteasy.core.Routes
import com.spliteasy.spliteasy.ui.auth.LoginScreen
import com.spliteasy.spliteasy.ui.auth.RegisterScreen
import com.spliteasy.spliteasy.ui.member.MemberNavScaffold
import com.spliteasy.spliteasy.ui.representative.RepresentativeHomeScreen

@Composable
fun AppNav(startDestination: String = Routes.LOGIN) {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onSuccess = { isRepresentative ->
                    nav.navigate(
                        if (isRepresentative) Routes.REP_HOME else Routes.MEM_HOME
                    ) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { nav.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(onDone = { nav.popBackStack() })
        }

        composable(Routes.REP_HOME) { RepresentativeHomeScreen() }
        composable(Routes.MEM_HOME) {
            MemberNavScaffold(
                onLogout = {
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
