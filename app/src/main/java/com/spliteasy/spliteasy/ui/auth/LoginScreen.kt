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
import androidx.compose.ui.graphics.Color
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
import com.spliteasy.spliteasy.ui.theme.*
import kotlinx.coroutines.launch

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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(FormColumnBg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                LanguageSwitchComponent(
                    modifier = Modifier.align(Alignment.TopEnd)
                )
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
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.login_subtitle),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.9f)
                    )
                )

                Spacer(Modifier.height(24.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                        .widthIn(max = 420.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F)),
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
                                color = Color.White,
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
                                    color = Color(0x26E53935),
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
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.login_card_no_account),
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                stringResource(R.string.login_card_create_account),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = BrandSecondary,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.clickable { onNavigateToRegister() }
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            stringResource(R.string.login_label_username),
                            color = Color.White,
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
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = TextMuted) },
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
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White,
                                focusedBorderColor = BorderColor,
                                unfocusedBorderColor = BorderColor.copy(alpha = 0.6f),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedLeadingIconColor = TextMuted,
                                unfocusedLeadingIconColor = TextMuted
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            )
                        )

                        Spacer(Modifier.height(14.dp))

                        Text(
                            stringResource(R.string.login_label_password),
                            color = Color.White,
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
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = TextMuted) },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    val icon = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility
                                    val label = if (showPassword) {
                                        stringResource(R.string.login_pass_hide)
                                    } else {
                                        stringResource(R.string.login_pass_show)
                                    }
                                    Icon(icon, contentDescription = label, tint = TextMuted)
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
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White,
                                focusedBorderColor = BorderColor,
                                unfocusedBorderColor = BorderColor.copy(alpha = 0.6f),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedLeadingIconColor = TextMuted,
                                unfocusedLeadingIconColor = TextMuted
                            ),
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
                                color = BrandSecondary,
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
                                color = Color(0xFF2C2C2C),
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
                                        color = BrandSecondary
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        stringResource(R.string.login_recaptcha_verifying),
                                        color = Color.White
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
                                containerColor = BrandPrimary,
                                contentColor = Color.White
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
                                        color = Color.White
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