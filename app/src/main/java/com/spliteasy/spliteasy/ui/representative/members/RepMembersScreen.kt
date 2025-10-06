package com.spliteasy.spliteasy.ui.representative.members

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/* Paleta consistente */
private val BrandPrimary = Color(0xFF1565C0)
private val BgMain       = Color(0xFF1A1A1A)
private val CardBg       = Color(0xFF2D2D2D)
private val Border       = Color(0xFF404040)
private val TextPri      = Color(0xFFF8F9FA)
private val TextSec      = Color(0xFFADB5BD)

@Composable
fun RepMembersScreen(vm: RepMembersViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    Surface(Modifier.fillMaxSize(), color = BgMain) {
        Column(Modifier.fillMaxSize()) {
            TopRow(onAddClick = { vm.openAddDialog(true) })

            when {
                ui.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandPrimary)
                }
                ui.error != null -> ErrorBox(ui.error ?: "Error", onRetry = vm::load)
                ui.members.isEmpty() -> EmptyBox(onAdd = { vm.openAddDialog(true) })
                else -> MembersList(
                    items = ui.members,
                    onDelete = { vm.deleteMember(it) }
                )
            }
        }
    }

    if (ui.showAddDialog) {
        AddMemberDialog(
            saving = ui.saving,
            onCancel = { vm.openAddDialog(false) },
            onSubmit = { email -> vm.addByEmail(email) }
        )
    }
}

/* ---------- Widgets ---------- */

@Composable
private fun TopRow(onAddClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(BgMain)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "Miembros del hogar",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TextPri,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                "Administra quién pertenece a este hogar.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSec)
            )
        }
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
        ) {
            Icon(Icons.Rounded.GroupAdd, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Añadir")
        }
    }
    Divider(color = Border, thickness = 1.dp)
}

@Composable
private fun MembersList(
    items: List<com.spliteasy.spliteasy.data.remote.dto.RawUserDto>,
    onDelete: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items.size) { idx ->
            val m = items[idx]
            Surface(
                color = CardBg,
                shape = RoundedCornerShape(14.dp),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(Border)
                )
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // avatar inicial
                    val initial = (m.username ?: m.email ?: "U").trim()
                        .ifEmpty { "U" }.first().uppercaseChar().toString()

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(BrandPrimary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            initial,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimary
                            )
                        )
                    }

                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            m.username ?: "—",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = TextPri,
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            m.email ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSec),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = { if (m.id != null) onDelete(m.id) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Eliminar", tint = Color(0xFFD32F2F))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddMemberDialog(
    saving: Boolean,
    onCancel: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            Button(onClick = { onSubmit(email.trim()) }, enabled = !saving && email.isNotBlank()) {
                if (saving) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp),
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("Añadir")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancel, enabled = !saving) {
                Text("Cancelar")
            }
        },
        title = { Text("Añadir miembro", color = TextPri) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ingresa el email del usuario que deseas añadir.", color = TextSec)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true
                )
            }
        },
        containerColor = CardBg,
        textContentColor = TextPri,
        titleContentColor = TextPri
    )
}

@Composable
private fun ErrorBox(message: String, onRetry: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Ups…", style = MaterialTheme.typography.titleLarge, color = Color(0xFFFF4D4F))
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = TextSec)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
        ) { Text("Reintentar") }
    }
}

@Composable
private fun EmptyBox(onAdd: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sin miembros", style = MaterialTheme.typography.titleLarge, color = TextPri)
        Spacer(Modifier.height(8.dp))
        Text(
            "Aún no has agregado miembros a este hogar.",
            style = MaterialTheme.typography.bodyMedium, color = TextSec
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAdd, colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)) {
            Text("Añadir el primero")
        }
    }
}
