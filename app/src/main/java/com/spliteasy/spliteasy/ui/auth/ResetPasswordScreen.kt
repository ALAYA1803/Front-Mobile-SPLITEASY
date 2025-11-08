package com.spliteasy.spliteasy.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.spliteasy.spliteasy.R

private val BgMain      = Color(0xFF2D2D2D)
private val CardBg      = Color(0xFF1B1E24)
private val CardBgElev  = Color(0xFF222632)
private val Border      = Color(0xFF2B2F3A)
private val TextPri     = Color(0xFFF3F4F6)
private val TextSec     = Color(0xFF9AA0A6)
private val BrandPrimary= Color(0xFF1565C0)
private val Danger      = Color(0xFFE53935)
private val Success     = Color(0xFF43A047)

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
            .background(BgMain)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.reset_pass_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TextPri,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.reset_pass_subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSec
                )
            )

            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
                    .widthIn(max = 420.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = BorderStroke(1.dp, Border)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        stringResource(R.string.reset_pass_card_title),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = TextPri,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    Text(
                        stringResource(R.string.reset_pass_label_new),
                        color = TextPri,
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
                                Text(error ?: "", color = Danger)
                            } else {
                                Text(stringResource(R.string.reset_pass_support_hint), color = TextSec)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPri,
                            unfocusedTextColor = TextPri,
                            cursorColor = BrandPrimary,
                            focusedBorderColor = Border,
                            unfocusedBorderColor = Border.copy(alpha = 0.6f),
                            errorBorderColor = Danger,
                            focusedContainerColor = CardBgElev,
                            unfocusedContainerColor = CardBgElev,
                            errorContainerColor = CardBgElev,
                            focusedLabelColor = TextSec,
                            unfocusedLabelColor = TextSec,
                            focusedTrailingIconColor = TextSec,
                            unfocusedTrailingIconColor = TextSec,
                            errorTrailingIconColor = Danger
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        stringResource(R.string.reset_pass_label_confirm),
                        color = TextPri,
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPri,
                            unfocusedTextColor = TextPri,
                            cursorColor = BrandPrimary,
                            focusedBorderColor = Border,
                            unfocusedBorderColor = Border.copy(alpha = 0.6f),
                            errorBorderColor = Danger,
                            focusedContainerColor = CardBgElev,
                            unfocusedContainerColor = CardBgElev,
                            errorContainerColor = CardBgElev,
                            focusedLabelColor = TextSec,
                            unfocusedLabelColor = TextSec,
                            focusedTrailingIconColor = TextSec,
                            unfocusedTrailingIconColor = TextSec,
                            errorTrailingIconColor = Danger
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { vm.submit { onDoneGoLogin() } },
                        enabled = !loading && vm.pass1.isNotBlank() && vm.pass2.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandPrimary,
                            contentColor = Color.White,
                            disabledContainerColor = CardBgElev,
                            disabledContentColor = TextSec
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
                        Text(success, color = Success)
                    }
                }
            }
        }
    }
}