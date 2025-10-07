package com.spliteasy.spliteasy.ui.member.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.graphics.SolidColor
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.imePadding

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.PaddingValues

private val BrandPrimary = Color(0xFF1565C0)
private val DangerColor  = Color(0xFFD32F2F)
private val BgMain       = Color(0xFF1A1A1A)
private val BgCard       = Color(0xFF2D2D2D)
private val Border       = Color(0xFF404040)
private val TextPri      = Color(0xFFF8F9FA)
private val TextSec      = Color(0xFFADB5BD)

@Composable
fun MembSettingsScreen(
    vm: MembSettingsViewModel = hiltViewModel(),
    onLogout: () -> Unit = {},
    onDeleted: () -> Unit = {}
) {
    val ui by vm.ui.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    var currentPwd by remember { mutableStateOf("") }
    var newPwd by remember { mutableStateOf("") }
    var confirmPwd by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showSnack(msg: String) {
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(message = msg, withDismissAction = true)
        }
    }

    Scaffold(
        containerColor = BgMain,
        snackbarHost = { SnackbarHost(hostState = snackbar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BgMain)
                .padding(padding)
                .imePadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Ajustes",
                    color = TextPri,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(2.dp))
                Text("Gestiona tu perfil y seguridad", color = TextSec, style = MaterialTheme.typography.bodyMedium)
            }

            // --- Perfil ---
            item {
                SettingsCard {
                    SectionHeader(icon = "👤", title = "Perfil", subtitle = "Actualiza tu nombre y correo")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ui.name,
                        onValueChange = vm::onNameChange,
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = ui.email,
                        onValueChange = vm::onEmailChange,
                        label = { Text("Correo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { vm.saveProfile { _, msg -> showSnack(msg) } },
                        enabled = ui.canSubmitProfile && !ui.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) { Text("Guardar cambios") }
                }
            }

            item {
                SettingsCard {
                    SectionHeader(icon = "🔒", title = "Seguridad", subtitle = "Cambia tu contraseña")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = currentPwd,
                        onValueChange = { currentPwd = it },
                        label = { Text("Contraseña actual") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = fieldColors()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPwd,
                        onValueChange = { newPwd = it },
                        label = { Text("Nueva contraseña") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = fieldColors()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = confirmPwd,
                        onValueChange = { confirmPwd = it },
                        label = { Text("Confirmar contraseña") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        supportingText = {
                            if (confirmPwd.isNotEmpty() && confirmPwd != newPwd)
                                Text("Las contraseñas no coinciden", color = DangerColor)
                        },
                        colors = fieldColors()
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (newPwd != confirmPwd) {
                                showSnack("Las contraseñas no coinciden")
                            } else {
                                vm.changePassword(currentPwd, newPwd) { ok, msg ->
                                    showSnack(msg)
                                    if (ok) { currentPwd = ""; newPwd = ""; confirmPwd = "" }
                                }
                            }
                        },
                        enabled = currentPwd.isNotBlank() && newPwd.isNotBlank() && confirmPwd.isNotBlank() && !ui.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
                    ) { Text("Cambiar contraseña") }
                }
            }

            item {
                SettingsCard(borderColor = DangerColor) {
                    SectionHeader(icon = "⚠️", title = "Zona de peligro", subtitle = "Acciones irreversibles")
                    Spacer(Modifier.height(8.dp))
                    Text("Eliminar tu cuenta borrará definitivamente tus datos.", color = TextSec)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerColor)
                    ) { Text("Eliminar cuenta") }
                }
            }

            item {
                OutlinedButton(
                    onClick = onLogout,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPri),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                ) { Text("Cerrar sesión") }
            }
            item { Spacer(Modifier.height(100.dp)) }
        }

        if (ui.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandPrimary)
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Eliminar cuenta", color = TextPri) },
                text = { Text("¿Seguro que deseas eliminar tu cuenta? Esta acción es irreversible.", color = TextSec) },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        vm.deleteAccount(onDeleted = onDeleted) { msg -> showSnack(msg) }
                    }) { Text("Eliminar", color = DangerColor) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar", color = TextPri) }
                },
                containerColor = BgCard
            )
        }
    }
}


@Composable
private fun SettingsCard(
    borderColor: Color = Border,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BgCard,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp, brush = SolidColor(borderColor))
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable private fun SectionHeader(icon: String, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(title, color = TextPri, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextSec, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BrandPrimary,
    unfocusedBorderColor = Border,
    focusedTextColor = TextPri,
    unfocusedTextColor = TextPri,
    cursorColor = BrandPrimary,
    focusedLabelColor = BrandPrimary,
    unfocusedLabelColor = TextSec
)

private suspend fun show(host: SnackbarHostState, msg: String) {
    host.currentSnackbarData?.dismiss()
    host.showSnackbar(message = msg, withDismissAction = true)
}
