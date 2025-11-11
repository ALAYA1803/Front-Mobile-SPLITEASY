package com.spliteasy.spliteasy.ui.representative.members

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spliteasy.spliteasy.R
import com.spliteasy.spliteasy.data.remote.dto.RawUserDto

@Composable
fun RepMembersScreen(vm: RepMembersViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            TopRow(onAddClick = { vm.openAddDialog(true) })

            when {
                ui.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                ui.error != null -> ErrorBox(ui.error ?: stringResource(R.string.member_home_error_generic), onRetry = vm::load)
                ui.members.isEmpty() -> EmptyBox(onAdd = { vm.openAddDialog(true) })
                else -> MembersList(
                    items = ui.members,
                    onDelete = { vm.deleteMember(it) }
                )
            }
        }
    }

    if (ui.showAddDialog) {
        AddMemberDialog(
            saving = ui.saving,
            onCancel = { vm.openAddDialog(false) },
            onSubmit = { email -> vm.addByEmail(email) }
        )
    }
}


@Composable
private fun TopRow(onAddClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.rep_members_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                stringResource(R.string.rep_members_subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Rounded.GroupAdd, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.rep_members_button_add))
        }
    }
    Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
}

@Composable
private fun MembersList(
    items: List<RawUserDto>,
    onDelete: (Long) -> Unit
) {
    val fallbackInitial = stringResource(R.string.member_home_member_initial_fallback)
    val fallbackDash = stringResource(R.string.common_fallback_dash)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items.size) { idx ->
            val m = items[idx]
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                border = ButtonDefaults.outlinedButtonBorder(
                    enabled = true
                ).copy(
                    width = 1.dp,
                    brush = SolidColor(MaterialTheme.colorScheme.outline)
                )
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val initial = (m.username ?: m.email ?: fallbackInitial).trim()
                        .ifEmpty { fallbackInitial }.first().uppercaseChar().toString()

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            initial,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            m.username ?: fallbackDash,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            m.email ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = { if (m.id != null) onDelete(m.id) }) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = stringResource(R.string.rep_members_cd_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddMemberDialog(
    saving: Boolean,
    onCancel: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            Button(onClick = { onSubmit(email.trim()) }, enabled = !saving && email.isNotBlank()) {
                if (saving) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(stringResource(R.string.rep_members_dialog_button_add))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancel, enabled = !saving) {
                Text(stringResource(R.string.rep_members_dialog_button_cancel))
            }
        },
        title = { Text(stringResource(R.string.rep_members_dialog_title), color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.rep_members_dialog_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.rep_members_dialog_label_email)) },
                    singleLine = true,
                    colors = fieldColors()
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        titleContentColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun ErrorBox(message: String, onRetry: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.common_error_oops), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) { Text(stringResource(R.string.common_retry)) }
    }
}

@Composable
private fun EmptyBox(onAdd: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.rep_members_empty_title), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.rep_members_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAdd, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
            Text(stringResource(R.string.rep_members_empty_button))
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