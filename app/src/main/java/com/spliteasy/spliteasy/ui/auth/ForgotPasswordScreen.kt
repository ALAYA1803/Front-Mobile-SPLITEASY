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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.spliteasy.spliteasy.R


private val BrandPrimary = Color(0xFF1565C0)
private val BgMain = Color(0xFF2D2D2D)
private val CardBg = Color(0xFF1B1E24)
private val CardBgElev = Color(0xFF222632)
private val Border = Color(0xFF2B2F3A)
private val TextPri = Color(0xFFF3F4F6)
private val TextSec = Color(0xFF9AA0A6)
private val Danger = Color(0xFFE53935)
private val Success = Color(0xFF43A047)

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
            .background(BgMain)
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
                        tint = TextSec
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.forgot_password_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextPri,
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
                colors = CardDefaults.cardColors(containerColor = CardBg),
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
                        color = TextPri,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        stringResource(R.string.forgot_password_card_subtitle),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSec,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    )

                    Spacer(Modifier.height(32.dp))

                    Text(
                        stringResource(R.string.forgot_password_email_label),
                        color = TextPri,
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
                                color = TextSec.copy(alpha = 0.6f),
                                fontSize = 15.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = TextSec
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 15.sp,
                            color = TextPri
                        ),
                        isError = (error != null && success == null),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPri,
                            unfocusedTextColor = TextPri,
                            cursorColor = BrandPrimary,
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = Border,
                            errorBorderColor = Danger,
                            focusedContainerColor = CardBgElev,
                            unfocusedContainerColor = CardBgElev,
                            errorContainerColor = CardBgElev,
                            focusedLeadingIconColor = BrandPrimary,
                            unfocusedLeadingIconColor = TextSec,
                            errorLeadingIconColor = Danger
                        )
                    )

                    if (error != null && success == null) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                error,
                                color = Danger,
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
                                color = Success,
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
                            containerColor = BrandPrimary,
                            contentColor = Color.White,
                            disabledContainerColor = BrandPrimary.copy(alpha = 0.5f),
                            disabledContentColor = Color.White.copy(alpha = 0.7f)
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
                        color = Border,
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
                            color = TextSec,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = stringResource(R.string.forgot_password_prompt_login_link),
                            color = BrandPrimary,
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