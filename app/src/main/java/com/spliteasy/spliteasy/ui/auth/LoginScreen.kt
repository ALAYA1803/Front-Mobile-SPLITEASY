package com.spliteasy.spliteasy.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spliteasy.spliteasy.R
import com.spliteasy.spliteasy.ui.member.settings.LanguageSwitchComponent
import com.spliteasy.spliteasy.ui.settings.ThemeViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material3.OutlinedIconButton
@Composable
fun LoginScreen(
    onSuccess: (Boolean) -> Unit,
    onNavigateToRegister: () -> Unit,
    onForgotPassword: () -> Unit,
    vm: LoginViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var triedSubmit by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(vm.error) {
        vm.error?.let { msg ->
            snackbarHostState.showSnackbar(message = msg, withDismissAction = true)
        }
    }
    val invalidCreds = vm.error == stringResource(R.string.login_vm_error_401)
    val usernameEmptyError = triedSubmit && username.isBlank()
    val passwordEmptyError = triedSubmit && password.isBlank()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ThemeSwitchComponent()
                    LanguageSwitchComponent()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = stringResource(R.string.login_logo_cd),
                    modifier = Modifier.size(84.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.login_subtitle),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(Modifier.height(24.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                        .widthIn(max = 420.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            stringResource(R.string.login_card_title),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        AnimatedVisibility(
                            visible = vm.error != null && !invalidCreds,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            vm.error?.let { mappedError ->
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ErrorOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            mappedError,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.login_card_no_account),
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                stringResource(R.string.login_card_create_account),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.clickable { onNavigateToRegister() }
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            stringResource(R.string.login_label_username),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                if (triedSubmit) vm.clearError()
                            },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            isError = usernameEmptyError || invalidCreds,
                            supportingText = {
                                when {
                                    usernameEmptyError -> Text(
                                        stringResource(R.string.login_error_username_empty),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    invalidCreds -> Text(
                                        stringResource(R.string.login_error_credentials_invalid),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            colors = fieldColors(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            )
                        )

                        Spacer(Modifier.height(14.dp))

                        Text(
                            stringResource(R.string.login_label_password),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                if (triedSubmit) vm.clearError()
                            },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    val icon = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility
                                    val label = if (showPassword) {
                                        stringResource(R.string.login_pass_hide)
                                    } else {
                                        stringResource(R.string.login_pass_show)
                                    }
                                    Icon(
                                        icon,
                                        contentDescription = label,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            isError = passwordEmptyError || invalidCreds,
                            supportingText = {
                                when {
                                    passwordEmptyError -> Text(
                                        stringResource(R.string.login_error_password_empty),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    invalidCreds -> Text(
                                        stringResource(R.string.login_error_credentials_invalid),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            colors = fieldColors(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (showPassword) KeyboardType.Text else KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    triedSubmit = true
                                    vm.clearError()
                                    scope.launch {
                                        if (username.isBlank() || password.isBlank()) return@launch
                                        vm.login(username, password)?.let(onSuccess)
                                    }
                                }
                            )
                        )

                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Text(
                                text = stringResource(R.string.login_forgot_password),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .clickable { onForgotPassword() }
                            )
                        }

                        AnimatedVisibility(
                            visible = vm.phase == LoginPhase.CHECKING_RECAPTCHA,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        stringResource(R.string.login_recaptcha_verifying),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                triedSubmit = true
                                vm.clearError()
                                scope.launch {
                                    if (username.isBlank() || password.isBlank()) return@launch
                                    vm.login(username, password)?.let(onSuccess)
                                }
                            },
                            enabled = !vm.loading && username.isNotBlank() && password.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            if (vm.loading) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    val label = when (vm.phase) {
                                        LoginPhase.CHECKING_RECAPTCHA -> stringResource(R.string.login_button_verifying_recaptcha)
                                        LoginPhase.SIGNING_IN -> stringResource(R.string.login_button_signing_in)
                                        else -> stringResource(R.string.login_button_processing)
                                    }
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            } else {
                                Text(
                                    text = stringResource(R.string.login_button_login),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
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
    focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
    disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSwitchComponent(
    modifier: Modifier = Modifier,
    vm: ThemeViewModel = hiltViewModel()
) {
    val currentTheme by vm.theme.collectAsState()
    val (icon, contentDescRes, onClickAction) = when (currentTheme) {
        "LIGHT" -> Triple(
            Icons.Rounded.LightMode,
            R.string.theme_switch_to_dark,
            { vm.setTheme("DARK") }
        )
        "DARK" -> Triple(
            Icons.Rounded.DarkMode,
            R.string.theme_switch_to_system,
            { vm.setTheme("SYSTEM") }
        )
        else -> Triple(
            Icons.Rounded.SettingsBrightness,
            R.string.theme_switch_to_light,
            { vm.setTheme("LIGHT") }
        )
    }
    OutlinedIconButton(
        onClick = onClickAction,
        modifier = modifier.size(40.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(contentDescRes),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

