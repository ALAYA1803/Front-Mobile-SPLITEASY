package com.spliteasy.spliteasy.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spliteasy.spliteasy.R
import com.spliteasy.spliteasy.ui.member.settings.LanguageSwitchComponent
import com.spliteasy.spliteasy.ui.settings.ThemeViewModel
import com.spliteasy.spliteasy.ui.theme.DangerColor
import com.spliteasy.spliteasy.ui.theme.SuccessColor

@Composable
fun ResetPasswordScreen(
    vm: ResetPasswordViewModel = hiltViewModel(),
    onDoneGoLogin: () -> Unit
) {
    val loading = vm.loading
    val error = vm.error
    val success = vm.success
    var show1 by remember { mutableStateOf(false) }
    var show2 by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 4.dp),
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
                text = stringResource(R.string.reset_pass_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.reset_pass_subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
                    .widthIn(max = 420.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        stringResource(R.string.reset_pass_card_title),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    Text(
                        stringResource(R.string.reset_pass_label_new),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Spacer(Modifier.height(6.dp))

                    val cdShowHide1 = if (show1) {
                        stringResource(R.string.reset_pass_cd_hide)
                    } else {
                        stringResource(R.string.reset_pass_cd_show)
                    }

                    OutlinedTextField(
                        value = vm.pass1,
                        onValueChange = vm::onPass1Change,
                        singleLine = true,
                        visualTransformation = if (show1) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { show1 = !show1 }) {
                                Icon(
                                    imageVector = if (show1) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = cdShowHide1
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        isError = (error != null && success == null),
                        supportingText = {
                            if (error != null && success == null) {
                                Text(error ?: "", color = DangerColor)
                            } else {
                                Text(stringResource(R.string.reset_pass_support_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors()
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        stringResource(R.string.reset_pass_label_confirm),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Spacer(Modifier.height(6.dp))

                    val cdShowHide2 = if (show2) {
                        stringResource(R.string.reset_pass_cd_hide)
                    } else {
                        stringResource(R.string.reset_pass_cd_show)
                    }

                    OutlinedTextField(
                        value = vm.pass2,
                        onValueChange = vm::onPass2Change,
                        singleLine = true,
                        visualTransformation = if (show2) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { show2 = !show2 }) {
                                Icon(
                                    imageVector = if (show2) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = cdShowHide2
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        isError = (error != null && success == null),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors()
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { vm.submit { onDoneGoLogin() } },
                        enabled = !loading && vm.pass1.isNotBlank() && vm.pass2.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        val buttonText = if (loading) {
                            stringResource(R.string.reset_pass_button_loading)
                        } else {
                            stringResource(R.string.reset_pass_button_submit)
                        }
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    if (success != null) {
                        Spacer(Modifier.height(10.dp))
                        Text(success, color = SuccessColor)
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
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    errorContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    errorBorderColor = MaterialTheme.colorScheme.error,
    errorLeadingIconColor = MaterialTheme.colorScheme.error,
    errorTrailingIconColor = MaterialTheme.colorScheme.error
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