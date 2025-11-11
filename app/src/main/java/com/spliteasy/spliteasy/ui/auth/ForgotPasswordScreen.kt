package com.spliteasy.spliteasy.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spliteasy.spliteasy.R
import com.spliteasy.spliteasy.ui.theme.DangerColor
import com.spliteasy.spliteasy.ui.theme.SuccessColor

@Composable
fun ForgotPasswordScreen(
    vm: ForgotPasswordViewModel = hiltViewModel(),
    onBackToLogin: () -> Unit,
    onGoToResetWithToken: (String) -> Unit
) {
    val loading = vm.loading
    val error = vm.error
    val success = vm.success
    val email = vm.email

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToLogin,
                    enabled = !loading
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.forgot_password_back_cd),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.forgot_password_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp
                    )
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp)
                ) {
                    Text(
                        stringResource(R.string.forgot_password_card_title),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        stringResource(R.string.forgot_password_card_subtitle),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    )

                    Spacer(Modifier.height(32.dp))

                    Text(
                        stringResource(R.string.forgot_password_email_label),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = vm::onEmailChange,
                        singleLine = true,
                        placeholder = {
                            Text(
                                stringResource(R.string.forgot_password_email_placeholder),
                                fontSize = 15.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        isError = (error != null && success == null),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors()
                    )

                    if (error != null && success == null) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                error,
                                color = DangerColor,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 13.sp
                                )
                            )
                        }
                    } else if (success != null) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                success,
                                color = SuccessColor,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {
                            vm.submit { token ->
                                if (!token.isNullOrBlank()) {
                                    onGoToResetWithToken(token)
                                }
                            }
                        },
                        enabled = !loading && email.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp
                        )
                    ) {
                        val buttonText = if (loading) {
                            stringResource(R.string.forgot_password_button_loading)
                        } else {
                            stringResource(R.string.forgot_password_button_submit)
                        }
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Divider(
                        color = MaterialTheme.colorScheme.outline,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.forgot_password_prompt_login),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = stringResource(R.string.forgot_password_prompt_login_link),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier.clickable(enabled = !loading) {
                                onBackToLogin()
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

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
    errorLeadingIconColor = MaterialTheme.colorScheme.error
)