package com.spliteasy.spliteasy.ui.representative.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spliteasy.spliteasy.ui.representative.home.create.RepCreateHouseholdViewModel
import androidx.compose.foundation.background

import androidx.compose.ui.res.stringResource
import com.spliteasy.spliteasy.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepCreateHouseholdScreen(
    onCancel: () -> Unit,
    onCreated: () -> Unit,
    vm: RepCreateHouseholdViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("PEN") }
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                stringResource(R.string.rep_create_title),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                stringResource(R.string.rep_create_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.rep_create_label_name)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading,
                colors = fieldColors()
            )

            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text(stringResource(R.string.rep_create_label_description)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading,
                minLines = 3,
                colors = fieldColors()
            )

            ExposedDropdownMenuBox(expanded = vm.menuExpanded, onExpandedChange = { vm.toggleMenu() }) {
                OutlinedTextField(
                    value = currency,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.rep_create_label_currency)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vm.menuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    enabled = !loading,
                    colors = fieldColors()
                )
                ExposedDropdownMenu(
                    expanded = vm.menuExpanded,
                    onDismissRequest = { vm.toggleMenu(false) },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    listOf("PEN","USD","EUR").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                currency = option
                                vm.toggleMenu(false)
                            }
                        )
                    }
                }
            }

            if (error != null) {
                Text(
                    error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !loading,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.rep_create_button_cancel))
                }
                Button(
                    onClick = {
                        vm.create(name.trim(), desc.trim(), currency.trim()) {
                            onCreated()
                        }
                    },
                    enabled = !loading && name.isNotBlank() && currency.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.rep_create_button_create))
                }
            }
        }
    }
}
@Composable private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surface,
)