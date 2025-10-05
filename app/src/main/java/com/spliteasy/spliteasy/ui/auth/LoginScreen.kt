package com.spliteasy.spliteasy.ui.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spliteasy.spliteasy.R
import com.spliteasy.spliteasy.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun LoginScreen(
    onSuccess: (Boolean) -> Unit,
    onNavigateToRegister: () -> Unit,
    vm: LoginViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(vm.error) {
        vm.error?.let { msg ->
            snackbarHostState.showSnackbar(message = msg, withDismissAction = true)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(FormColumnBg)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "SplitEasy",
                    modifier = Modifier.size(84.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "SplitEasy",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Divide gastos sin dolor de cabeza.",
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
                            "Iniciar sesión",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "¿No tienes cuenta?",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Crear cuenta",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = BrandSecondary,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.clickable { onNavigateToRegister() }
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // === Usuario ===
                        Text(
                            "Usuario",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = TextMuted) },
                            // ❌ Quita .height(56.dp)
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge, // para que ambos se vean iguales
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White,
                                focusedBorderColor = BorderColor,
                                unfocusedBorderColor = BorderColor.copy(alpha = 0.6f),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedLeadingIconColor = TextMuted,
                                unfocusedLeadingIconColor = TextMuted,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White
                            )
                        )

                        Spacer(Modifier.height(14.dp))

// === Contraseña ===
                        Text(
                            "Contraseña",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = TextMuted) },

                            // 👁️ Mostrar/ocultar contraseña
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    val icon = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility
                                    val label = if (showPassword) "Ocultar" else "Mostrar"
                                    Icon(icon, contentDescription = label, tint = TextMuted)
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),

                            // ❌ Quita .height(56.dp) para evitar recortes
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            isError = (vm.error == "Usuario o contraseña incorrectos."),
                            supportingText = {
                                if (vm.error == "Usuario o contraseña incorrectos.") {
                                    Text("Usuario o contraseña incorrectos.", color = MaterialTheme.colorScheme.error)
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
                                unfocusedLeadingIconColor = TextMuted,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (showPassword) KeyboardType.Text else KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    // mismo click del botón
                                    vm.clearError()
                                    scope.launch {
                                        vm.login(username, password)?.let(onSuccess)
                                    }
                                }
                            )
                        )


                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Text(
                                "¿Olvidaste tu contraseña?",
                                style = MaterialTheme.typography.bodySmall.copy(color = BrandSecondary),
                                modifier = Modifier.clickable {
                                    Toast.makeText(ctx, "Recuperar contraseña", Toast.LENGTH_SHORT).show()
                                }
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
                                        "Protegido por reCAPTCHA • verificando…",
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                vm.clearError()
                                scope.launch {
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
                                        LoginPhase.CHECKING_RECAPTCHA -> "Verificando reCAPTCHA…"
                                        LoginPhase.SIGNING_IN -> "Comprobando credenciales…"
                                        else -> "Procesando…"
                                    }
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            } else {
                                Text(
                                    text = "Ingresar",
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
