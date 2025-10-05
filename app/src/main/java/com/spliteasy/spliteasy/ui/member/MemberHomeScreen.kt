package com.spliteasy.spliteasy.ui.member

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MemberHomeScreen(
    onAddExpense: () -> Unit,
    onOpenExpense: (Long) -> Unit,
    vm: MemberHomeViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    when (val s = state) {
        is MemberHomeUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is MemberHomeUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Ups: ${s.message}")
                Spacer(Modifier.height(8.dp))
                Button(onClick = { vm.load(forceRefresh = true) }) { Text("Reintentar") }
            }
        }
        is MemberHomeUiState.Ready -> Column(Modifier.fillMaxSize().padding(16.dp)) {
            BalanceCard(net = s.balance.net, owe = s.balance.iOwe, owed = s.balance.meOwe)
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Últimos gastos", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onAddExpense) { Text("Añadir gasto") }
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn {
                items(s.recent) { e ->
                    ExpenseRow(
                        title = e.description,
                        amount = e.amount,
                        onClick = { onOpenExpense(e.id) }
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun BalanceCard(net: Double, owe: Double, owed: Double) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Tu saldo")
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Neto: S/ %.2f".format(net),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(8.dp))
            Text("Debes: S/ %.2f".format(owe))
            Text("Te deben: S/ %.2f".format(owed))
        }
    }
}

@Composable
private fun ExpenseRow(title: String, amount: Double, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text("S/ %.2f".format(amount)) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)   // <- aquí el click
            .padding(vertical = 4.dp)
    )
}

/** placeholders */
@Composable fun ExpensesScreen(onAddExpense: () -> Unit, onOpenExpense: (Long) -> Unit) {}
@Composable fun MemberProfileScreen(onLogout: () -> Unit) {}
@Composable fun AddExpenseScreen(onDone: () -> Unit) {}
@Composable fun ExpenseDetailScreen(expenseId: Long, onBack: () -> Unit) {}
