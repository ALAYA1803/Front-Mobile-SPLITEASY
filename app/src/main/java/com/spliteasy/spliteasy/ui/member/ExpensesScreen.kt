package com.spliteasy.spliteasy.ui.member

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExpensesScreen(
    onAddExpense: () -> Unit,
    onOpenExpense: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Lista de Gastos",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(16.dp))
        Text("Pantalla en construcción")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAddExpense) {
            Text("Añadir Gasto")
        }
    }
}