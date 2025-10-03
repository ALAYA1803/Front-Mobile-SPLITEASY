package com.spliteasy.spliteasy.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.hilt.navigation.compose.hiltViewModel
import com.spliteasy.spliteasy.ui.theme.*
import androidx.compose.ui.focus.onFocusChanged

@Composable
fun RegisterScreen(
    onDone: () -> Unit,
    vm: RegisterViewModel = hiltViewModel()
) {
    val ctx = LocalContext.current

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var income by remember { mutableStateOf("") }

    // Dropdown rol (sin APIs experimentales)
    var expanded by remember { mutableStateOf(false) }
    var role by remember { mutableStateOf<String?>(null) }
    var fieldSize by remember { mutableStateOf(Size.Zero) }
    val density = LocalDensity.current
    val menuWidth = with(density) { fieldSize.width.toDp() }

    val pwdMismatch = password.isNotEmpty() && repeatPassword.isNotEmpty() && password != repeatPassword
    val incomeNumber = income.toDoubleOrNull() ?: -1.0
    val canSubmit = username.isNotBlank() &&
            email.isNotBlank() &&
            password.length >= 6 &&
            !pwdMismatch &&
            incomeNumber >= 0.0 &&
            role != null &&
            !vm.loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FormColumnBg)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Crear cuenta",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Únete y divide gastos sin dolor de cabeza.",
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
                Column(Modifier.padding(20.dp)) {

                    Label("Usuario")
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Person, null, tint = TextMuted) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                    Spacer(Modifier.height(12.dp))

                    Label("Correo")
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Email, null, tint = TextMuted) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                    Spacer(Modifier.height(12.dp))

                    Label("Contraseña (mín. 6)")
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Lock, null, tint = TextMuted) },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                    Spacer(Modifier.height(12.dp))

                    Label("Repite la contraseña")
                    OutlinedTextField(
                        value = repeatPassword,
                        onValueChange = { repeatPassword = it },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Lock, null, tint = TextMuted) },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = pwdMismatch,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                    if (pwdMismatch) {
                        Spacer(Modifier.height(6.dp))
                        Text("Las contraseñas no coinciden.", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.height(12.dp))

                    Label("Ingreso (S/.)")
                    OutlinedTextField(
                        value = income,
                        onValueChange = { if (it.length <= 10) income = it },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Money, null, tint = TextMuted) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                    if (income.isNotEmpty() && incomeNumber < 0.0) {
                        Spacer(Modifier.height(6.dp))
                        Text("Debe ser un número mayor o igual a 0.", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.height(12.dp))

                    Label("Rol")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.TopStart)
                    ) {
                        OutlinedTextField(
                            value = when (role) {
                                "ROLE_MIEMBRO" -> "Miembro"
                                "ROLE_REPRESENTANTE" -> "Representante"
                                else -> ""
                            },
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = { Icon(Icons.Outlined.Person, null, tint = TextMuted) },
                            trailingIcon = {
                                Icon(
                                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.clickable { expanded = !expanded }
                                )
                            },
                            placeholder = { Text("Selecciona rol", color = TextMuted) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coords -> fieldSize = coords.size.toSize() }
                                .onFocusChanged { st -> if (st.isFocused) expanded = true },
                            colors = fieldColors()
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.width(menuWidth)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Miembro") },
                                onClick = {
                                    role = "ROLE_MIEMBRO"
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Representante") },
                                onClick = {
                                    role = "ROLE_REPRESENTANTE"
                                    expanded = false
                                }
                            )
                        }
                    }

                    vm.error?.let { msg ->
                        Spacer(Modifier.height(12.dp))
                        Text(msg, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = {
                            vm.register(
                                username = username.trim(),
                                email = email.trim(),
                                password = password,
                                income = incomeNumber,
                                role = role!!
                            ) { ok ->
                                if (ok) {
                                    Toast.makeText(ctx, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                    onDone()
                                }
                            }
                        },
                        enabled = canSubmit,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            if (vm.loading) "Creando..." else "Crear cuenta",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "¿Ya tienes cuenta? ",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                        )
                        Text(
                            "Inicia sesión",
                            style = MaterialTheme.typography.bodySmall.copy(color = BrandSecondary, fontWeight = FontWeight.Medium),
                            modifier = Modifier.clickable { onDone() }
                        )
                    }
                }
            }
        }
    }
}

@Composable private fun Label(text: String) {
    Text(text, color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
    Spacer(Modifier.height(6.dp))
}

@Composable private fun fieldColors() = OutlinedTextFieldDefaults.colors(
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
    unfocusedLabelColor = Color.White,
    focusedPlaceholderColor = TextMuted,
    unfocusedPlaceholderColor = TextMuted
)
