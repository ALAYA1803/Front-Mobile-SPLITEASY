package com.spliteasy.spliteasy.ui.member.settings

import android.util.Log
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.spliteasy.spliteasy.ui.settings.LanguageViewModel
import androidx.compose.ui.res.stringResource
import com.spliteasy.spliteasy.R
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.animation.core.tween
import kotlinx.coroutines.delay

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
    val langVm: LanguageViewModel = hiltViewModel()
    val isRestarting by langVm.isRestarting.collectAsState()
    val context = LocalContext.current

    fun showSnack(msg: String) {
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(message = msg, withDismissAction = true)
        }
    }

    LaunchedEffect(isRestarting) {
        if (isRestarting) {
            delay(200) // Delay más corto
            (context as? Activity)?.let { activity ->
                // Resetear el estado antes de recrear
                langVm.onLanguageApplied()
                activity.recreate()
            }
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
                    stringResource(R.string.settings_title),
                    color = TextPri,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    stringResource(R.string.settings_subtitle),
                    color = TextSec,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            item {
                SettingsCard {
                    SectionHeader(
                        icon = "👤",
                        title = stringResource(R.string.settings_profile_title),
                        subtitle = stringResource(R.string.settings_profile_subtitle)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ui.name,
                        onValueChange = vm::onNameChange,
                        label = { Text(stringResource(R.string.settings_profile_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = ui.email,
                        onValueChange = vm::onEmailChange,
                        label = { Text(stringResource(R.string.settings_profile_email)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { vm.saveProfile { _, msg -> showSnack(msg) } },
                        enabled = ui.canSubmitProfile && !ui.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) { Text(stringResource(R.string.settings_profile_save)) }
                }
            }

            item {
                SettingsCard {
                    SectionHeader(
                        icon = "🔒",
                        title = stringResource(R.string.settings_security_title),
                        subtitle = stringResource(R.string.settings_security_subtitle)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = currentPwd,
                        onValueChange = { currentPwd = it },
                        label = { Text(stringResource(R.string.settings_security_current_pass)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = fieldColors()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPwd,
                        onValueChange = { newPwd = it },
                        label = { Text(stringResource(R.string.settings_security_new_pass)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = fieldColors()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = confirmPwd,
                        onValueChange = { confirmPwd = it },
                        label = { Text(stringResource(R.string.settings_security_confirm_pass)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        supportingText = {
                            if (confirmPwd.isNotEmpty() && confirmPwd != newPwd)
                                Text(
                                    stringResource(R.string.settings_security_pass_mismatch),
                                    color = DangerColor
                                )
                        },
                        colors = fieldColors()
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            vm.changePassword(currentPwd, newPwd, confirmPwd) { ok, msg ->
                                showSnack(msg)
                                if (ok) {
                                    currentPwd = ""
                                    newPwd = ""
                                    confirmPwd = ""
                                }
                            }
                        },
                        enabled = currentPwd.isNotBlank() && newPwd.isNotBlank() && confirmPwd.isNotBlank() && !ui.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
                    ) { Text(stringResource(R.string.settings_security_save)) }
                }
            }

            item {
                SettingsCard(borderColor = DangerColor) {
                    SectionHeader(
                        icon = "⚠️",
                        title = stringResource(R.string.settings_danger_title),
                        subtitle = stringResource(R.string.settings_danger_subtitle)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.settings_danger_desc),
                        color = TextSec
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerColor)
                    ) { Text(stringResource(R.string.settings_danger_delete_account)) }
                }
            }
            item {
                SettingsCard {
                    SectionHeader(icon = "🌎", title = "Idioma", subtitle = "Selecciona tu idioma")
                    Spacer(Modifier.height(12.dp))
                    LanguageSwitchComponent()
                }
            }
            item { Spacer(Modifier.height(100.dp)) }
        }

        if (ui.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandPrimary)
            }
        }
        AnimatedVisibility(
            visible = isRestarting,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgMain)
                    .systemBarsPadding()
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandPrimary)
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.settings_dialog_delete_title), color = TextPri) },
                text = { Text(stringResource(R.string.settings_dialog_delete_text), color = TextSec) },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        vm.deleteAccount(onDeleted = onDeleted) { msg -> showSnack(msg) }
                    }) { Text(stringResource(R.string.settings_dialog_delete_confirm), color = DangerColor) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(R.string.settings_dialog_delete_cancel), color = TextPri)
                    }
                },
                containerColor = BgCard
            )
        }
    }
}
@Composable
fun LanguageSwitchComponent(
    modifier: Modifier = Modifier,
    vm: LanguageViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentLang by vm.currentLanguageFlow.collectAsState(initial = "es")
    val isRestarting by vm.isRestarting.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    Log.d("LanguageSetup", "[Composable] El componente se actualizó. Idioma actual del Flow: $currentLang")
    val buttonText = when (currentLang) {
        "en" -> stringResource(R.string.language_current_en)
        else -> stringResource(R.string.language_current_es)
    }
    LaunchedEffect(currentLang) {
        if (isRestarting) {
            delay(400)
            vm.onLanguageApplied()
        }
    }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(buttonText)
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = stringResource(R.string.language_select)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF2D2D2D))
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.language_current_es), color = Color.White) },
                onClick = {
                    vm.setLanguage("es")
                    expanded = false
                },
                enabled = currentLang != "es"
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.language_current_en), color = Color.White) },
                onClick = {
                    vm.setLanguage("en")
                    expanded = false
                },
                enabled = currentLang != "en"
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