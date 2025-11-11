package com.spliteasy.spliteasy.ui.auth

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import com.spliteasy.spliteasy.R
import com.spliteasy.spliteasy.ui.member.settings.LanguageSwitchComponent
import com.spliteasy.spliteasy.ui.settings.ThemeViewModel

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
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThemeSwitchComponent()
            LanguageSwitchComponent()
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                stringResource(R.string.register_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.register_subtitle),
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
                Column(Modifier.padding(20.dp)) {

                    Label(stringResource(R.string.register_label_username))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Person, null) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                    Spacer(Modifier.height(12.dp))

                    Label(stringResource(R.string.register_label_email))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Email, null) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                    Spacer(Modifier.height(12.dp))

                    Label(stringResource(R.string.register_label_password_min_6))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Lock, null) },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                    Spacer(Modifier.height(12.dp))

                    Label(stringResource(R.string.register_label_password_repeat))
                    OutlinedTextField(
                        value = repeatPassword,
                        onValueChange = { repeatPassword = it },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Lock, null) },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = pwdMismatch,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                    if (pwdMismatch) {
                        Spacer(Modifier.height(6.dp))
                        Text(stringResource(R.string.register_error_password_mismatch), color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.height(12.dp))

                    Label(stringResource(R.string.register_label_income))
                    OutlinedTextField(
                        value = income,
                        onValueChange = { if (it.length <= 10) income = it },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Money, null) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                    if (income.isNotEmpty() && incomeNumber < 0.0) {
                        Spacer(Modifier.height(6.dp))
                        Text(stringResource(R.string.register_error_income_invalid), color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.height(12.dp))

                    Label(stringResource(R.string.register_label_role))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.TopStart)
                    ) {
                        val roleText = when (role) {
                            "ROLE_MIEMBRO" -> stringResource(R.string.register_role_member)
                            "ROLE_REPRESENTANTE" -> stringResource(R.string.register_role_representative)
                            else -> ""
                        }

                        OutlinedTextField(
                            value = roleText,
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = { Icon(Icons.Outlined.Person, null) },
                            trailingIcon = {
                                Icon(
                                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { expanded = !expanded }
                                )
                            },
                            placeholder = { Text(stringResource(R.string.register_placeholder_role)) },
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
                            modifier = Modifier
                                .width(menuWidth)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.register_role_member), color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    role = "ROLE_MIEMBRO"
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.register_role_representative), color = MaterialTheme.colorScheme.onSurface) },
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

                    val successMsg = stringResource(R.string.register_success_toast)

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
                                    Toast.makeText(ctx, successMsg, Toast.LENGTH_SHORT).show()
                                    onDone()
                                }
                            }
                        },
                        enabled = canSubmit,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        val buttonText = if (vm.loading) {
                            stringResource(R.string.register_button_loading)
                        } else {
                            stringResource(R.string.register_button_submit)
                        }
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            stringResource(R.string.register_prompt_login),
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
                        )
                        Text(
                            stringResource(R.string.register_prompt_login_link),
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium),
                            modifier = Modifier.clickable { onDone() }
                        )
                    }
                }
            }
        }
    }
}

@Composable private fun Label(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
    )
    Spacer(Modifier.height(6.dp))
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
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
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