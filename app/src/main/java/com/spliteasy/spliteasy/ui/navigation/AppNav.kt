package com.spliteasy.spliteasy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spliteasy.spliteasy.core.Routes
import com.spliteasy.spliteasy.data.local.TokenDataStore
import com.spliteasy.spliteasy.ui.auth.LoginScreen
import com.spliteasy.spliteasy.ui.auth.RegisterScreen
import com.spliteasy.spliteasy.ui.member.MemberNavRoot
import com.spliteasy.spliteasy.ui.representative.RepresentativeNavRoot
import kotlinx.coroutines.launch

@Composable
fun AppNav(startDestination: String = Routes.LOGIN) {
    val nav    = rememberNavController()
    val ctx    = LocalContext.current
    val scope  = rememberCoroutineScope()
    val tokenStore = remember { TokenDataStore(ctx.applicationContext) }

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
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = { nav.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(onDone = { nav.popBackStack() })
        }

        composable(Routes.REP_HOME) {
            RepresentativeNavRoot(
                onLogout = {
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MEM_HOME) {
            MemberNavRoot(
                onLogout = {
                    scope.launch {
                        tokenStore.clear()
                        nav.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}
