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
import com.spliteasy.spliteasy.ui.settings.ThemeViewModel
import kotlinx.coroutines.delay

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
            delay(200)
            (context as? Activity)?.let { activity ->
                langVm.onLanguageApplied()
                activity.recreate()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .imePadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.settings_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    stringResource(R.string.settings_subtitle),
                    color = MaterialTheme.colorScheme.onBackground,
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
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
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
                                    color = MaterialTheme.colorScheme.error
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
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) { Text(stringResource(R.string.settings_security_save)) }
                }
            }

            item {
                SettingsCard(borderColor = MaterialTheme.colorScheme.error) {
                    SectionHeader(
                        icon = "⚠️",
                        title = stringResource(R.string.settings_danger_title),
                        subtitle = stringResource(R.string.settings_danger_subtitle)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.settings_danger_desc),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text(stringResource(R.string.settings_danger_delete_account)) }
                }
            }
            item {
                SettingsCard {
                    SectionHeader(
                        icon = "🌎",
                        title = stringResource(R.string.settings_language_title),
                        subtitle = stringResource(R.string.settings_language_subtitle)
                    )
                    Spacer(Modifier.height(12.dp))
                    LanguageSwitchComponent()
                }
            }
            item {
                SettingsCard {
                    SectionHeader(
                        icon = "🌗",
                        title = stringResource(R.string.theme_title),
                        subtitle = stringResource(R.string.theme_subtitle)
                    )
                    Spacer(Modifier.height(12.dp))
                    ThemeSwitchComponent()
                }
            }
            item { Spacer(Modifier.height(100.dp)) }
        }

        if (ui.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                    .background(MaterialTheme.colorScheme.background)
                    .systemBarsPadding()
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.settings_dialog_delete_title), color = MaterialTheme.colorScheme.onSurface) },
                text = { Text(stringResource(R.string.settings_dialog_delete_text), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        vm.deleteAccount(onDeleted = onDeleted) { msg -> showSnack(msg) }
                    }) { Text(stringResource(R.string.settings_dialog_delete_confirm), color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(R.string.settings_dialog_delete_cancel), color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
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
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
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
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.language_current_es), color = MaterialTheme.colorScheme.onSurface) },
                onClick = {
                    vm.setLanguage("es")
                    expanded = false
                },
                enabled = currentLang != "es"
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.language_current_en), color = MaterialTheme.colorScheme.onSurface) },
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
    borderColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.outline,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
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
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
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
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
)

private suspend fun show(host: SnackbarHostState, msg: String) {
    host.currentSnackbarData?.dismiss()
    host.showSnackbar(message = msg, withDismissAction = true)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSwitchComponent(
    modifier: Modifier = Modifier,
    vm: ThemeViewModel = hiltViewModel()
) {
    val currentTheme by vm.theme.collectAsState()

    val options = listOf(
        "LIGHT" to stringResource(R.string.theme_light),
        "SYSTEM" to stringResource(R.string.theme_system),
        "DARK" to stringResource(R.string.theme_dark)
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (key, label) ->
            FilterChip(
                selected = (currentTheme == key),
                onClick = { vm.setTheme(key) },
                label = { Text(label) },
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}