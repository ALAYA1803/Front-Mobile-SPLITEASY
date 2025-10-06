package com.spliteasy.spliteasy.ui.member

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.spliteasy.spliteasy.core.Routes
import androidx.compose.foundation.layout.padding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberNavScaffold(
    onLogout: () -> Unit,
    startDestination: String = Routes.MEM_HOME
) {
    val nav = rememberNavController()
    val items = listOf(
        Routes.MEM_HOME to Icons.Filled.Home,
        Routes.MEM_EXPENSES to Icons.Filled.ReceiptLong,
        Routes.MEM_PROFILE to Icons.Filled.AccountCircle
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SplitEasy") },
                actions = {
                    TextButton(onClick = onLogout) { Text("Salir") }
                }
            )
        },
        floatingActionButton = {
            val route = currentRoute(nav)
            if (route == Routes.MEM_HOME || route == Routes.MEM_EXPENSES) {
                FloatingActionButton(onClick = { nav.navigate(Routes.ADD_EXPENSE) }) {
                    Icon(Icons.Filled.ReceiptLong, contentDescription = null)
                }
            }
        },
        bottomBar = {
            NavigationBar {
                items.forEach { (route, icon) ->
                    val selected = currentRoute(nav) == route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) nav.navigate(route) {
                                popUpTo(Routes.MEM_HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(icon, null) },
                        label = { Text(when(route){
                            Routes.MEM_HOME -> "Inicio"
                            Routes.MEM_EXPENSES -> "Gastos"
                            else -> "Perfil"
                        }) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = nav,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.MEM_HOME) {
                MemberHomeScreen(
                    onAddExpense = { nav.navigate(Routes.ADD_EXPENSE) },
                    onOpenExpense = { id -> nav.navigate("expense_detail/$id") }
                )
            }
            composable(Routes.MEM_EXPENSES) {
                ExpensesScreen(
                    onAddExpense = { nav.navigate(Routes.ADD_EXPENSE) },
                    onOpenExpense = { id -> nav.navigate("expense_detail/$id") }
                )
            }
            composable(Routes.MEM_PROFILE) { MemberProfileScreen(onLogout = onLogout) }
            composable(Routes.ADD_EXPENSE) { AddExpenseScreen(onDone = { nav.popBackStack() }) }
            composable("${Routes.EXPENSE_DETAIL}/{expenseId}") { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getString("expenseId")?.toLongOrNull() ?: 0L
                ExpenseDetailScreen(
                    expenseId = expenseId,
                    onBack = { nav.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun currentRoute(nav: NavHostController): String? =
    nav.currentBackStackEntryAsState().value?.destination?.route
