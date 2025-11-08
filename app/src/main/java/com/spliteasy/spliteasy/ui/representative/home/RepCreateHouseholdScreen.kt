package com.spliteasy.spliteasy.ui.representative.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spliteasy.spliteasy.ui.representative.home.create.RepCreateHouseholdViewModel


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment

import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.util.*
import androidx.compose.ui.res.stringResource
import com.spliteasy.spliteasy.R

private val BrandPrimary = Color(0xFF1565C0)
private val BgMain       = Color(0xFF1A1A1A)
private val CardBg       = Color(0xFF2D2D2D)
private val TextPri      = Color(0xFFF8F9FA)
private val TextSec      = Color(0xFFADB5BD)

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

    Surface(Modifier.fillMaxSize(), color = BgMain) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.rep_create_title), color = TextPri, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text(stringResource(R.string.rep_create_subtitle), color = TextSec)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.rep_create_label_name), color = TextSec) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardBg,
                    unfocusedContainerColor = CardBg,
                    disabledContainerColor = CardBg,
                    focusedIndicatorColor = BrandPrimary,
                    unfocusedIndicatorColor = CardBg,
                    disabledIndicatorColor = CardBg,
                    cursorColor = BrandPrimary,
                    focusedTextColor = TextPri,
                    unfocusedTextColor = TextPri,
                    disabledTextColor = TextPri
                )
            )

            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text(stringResource(R.string.rep_create_label_description), color = TextSec) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading,
                minLines = 3,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardBg,
                    unfocusedContainerColor = CardBg,
                    disabledContainerColor = CardBg,
                    focusedIndicatorColor = BrandPrimary,
                    unfocusedIndicatorColor = CardBg,
                    disabledIndicatorColor = CardBg,
                    cursorColor = BrandPrimary,
                    focusedTextColor = TextPri,
                    unfocusedTextColor = TextPri,
                    disabledTextColor = TextPri
                )
            )

            ExposedDropdownMenuBox(expanded = vm.menuExpanded, onExpandedChange = { vm.toggleMenu() }) {
                OutlinedTextField(
                    value = currency,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.rep_create_label_currency), color = TextSec) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vm.menuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    enabled = !loading,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = CardBg,
                        unfocusedContainerColor = CardBg,
                        disabledContainerColor = CardBg,
                        focusedIndicatorColor = BrandPrimary,
                        unfocusedIndicatorColor = CardBg,
                        disabledIndicatorColor = CardBg,
                        cursorColor = BrandPrimary,
                        focusedTextColor = TextPri,
                        unfocusedTextColor = TextPri,
                        disabledTextColor = TextPri
                    )
                )
                ExposedDropdownMenu(expanded = vm.menuExpanded, onDismissRequest = { vm.toggleMenu(false) }) {
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
                Text(error!!, color = Color(0xFFFF4D4F), style = MaterialTheme.typography.bodyMedium)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onCancel, enabled = !loading) {
                    Text(stringResource(R.string.rep_create_button_cancel))
                }
                Button(
                    onClick = {
                        vm.create(name.trim(), desc.trim(), currency.trim()) {
                            onCreated()
                        }
                    },
                    enabled = !loading && name.isNotBlank() && currency.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    if (loading) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp), color = Color.White)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.rep_create_button_create))
                }
            }
        }
    }
}