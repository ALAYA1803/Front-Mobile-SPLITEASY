package com.spliteasy.spliteasy.ui.member.contribs

import android.content.Context
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spliteasy.spliteasy.R
import com.spliteasy.spliteasy.ui.theme.DangerColor
import com.spliteasy.spliteasy.ui.theme.InfoColor
import com.spliteasy.spliteasy.ui.theme.SuccessColor
import com.spliteasy.spliteasy.ui.theme.WarningColor
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody


@Composable
fun MembContribsScreen(
    currentUserId: Long,
    vm: MembContribsViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()
    val ctx = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(currentUserId) { vm.load(currentUserId) }
    val snackSuccess = stringResource(R.string.memb_contribs_toast_upload_success)
    val snackFail = stringResource(R.string.memb_contribs_toast_upload_fail)
    val snackReadFail = stringResource(R.string.memb_contribs_toast_read_fail)

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val mcId = (ui as? ContribsUiState.Ready)?.dialogForContrib?.mc?.id

        vm.closePaymentDialog()

        if (uri != null && mcId != null) {
            multipartFromUri(ctx, uri)?.let { part ->
                vm.uploadReceipt(mcId, part) { ok ->
                    scope.launch {
                        snackbar.showSnackbar(if (ok) snackSuccess else snackFail)
                    }
                }
            } ?: scope.launch { snackbar.showSnackbar(snackReadFail) }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        when (val s = ui) {
            is ContribsUiState.Loading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

            is ContribsUiState.Error -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { Text(s.message, color = MaterialTheme.colorScheme.onSurfaceVariant) }

            is ContribsUiState.Ready -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            stringResource(R.string.memb_contribs_title),
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.memb_contribs_subtitle),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    items(s.rows, key = { it.mc.id }) { row ->
                        ContribCard(
                            row = row,
                            onPayClick = { vm.openPaymentDialog(row) }
                        )
                    }

                    if (s.rows.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stringResource(R.string.memb_contribs_empty_list), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
                if (s.dialogForContrib != null) {
                    PaymentDialog(
                        row = s.dialogForContrib,
                        onDismiss = vm::closePaymentDialog,
                        onUploadClick = {
                            filePicker.launch("*/*")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContribCard(
    row: ContribRow,
    onPayClick: () -> Unit
) {
    val fallbackDash = stringResource(R.string.common_fallback_dash)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = row.billDescription ?: stringResource(R.string.memb_contribs_fallback_bill),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                StatusPill(row.statusUi)
            }

            Spacer(Modifier.height(8.dp))
            HorizontalLine()

            val strategyText = when(row.strategy?.uppercase()) {
                "EQUAL" -> stringResource(R.string.memb_contribs_strategy_equal)
                "INCOME_BASED" -> stringResource(R.string.memb_contribs_strategy_income)
                else -> row.strategy ?: fallbackDash
            }

            Labeled(stringResource(R.string.memb_contribs_label_description), row.contribDescription ?: fallbackDash)
            Labeled(stringResource(R.string.memb_contribs_label_strategy), strategyText)
            Labeled(stringResource(R.string.memb_contribs_label_bill_date), row.billDate ?: fallbackDash)
            Labeled(stringResource(R.string.memb_contribs_label_due_date), row.dueDate ?: fallbackDash)
            Labeled(stringResource(R.string.memb_contribs_label_bill_amount), row.billAmount?.let { "S/ %.2f".format(it) } ?: fallbackDash)
            Labeled(stringResource(R.string.memb_contribs_label_amount_to_pay), "S/ %.2f".format(row.mc.amount))

            Spacer(Modifier.height(10.dp))

            val canPay = row.statusUi != "PAGADO"
            val buttonText = when (row.statusUi) {
                "EN_REVISION" -> stringResource(R.string.memb_contribs_button_replace)
                "PENDIENTE", "RECHAZADO" -> stringResource(R.string.memb_contribs_button_pay)
                else -> ""
            }

            if (canPay) {
                Button(
                    onClick = onPayClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (row.statusUi == "EN_REVISION") InfoColor else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(buttonText)
                }
            }
        }
    }
}
@Composable
private fun PaymentDialog(
    row: ContribRow,
    onDismiss: () -> Unit,
    onUploadClick: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackCopied = stringResource(R.string.memb_contribs_toast_copied)
    val snackbar = remember { SnackbarHostState() }
    val qrBitmap = remember(row.qr) {
        try {
            val bytes = Base64.decode(row.qr, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.memb_contribs_dialog_title)) },
        text = {
            Box {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Código QR",
                            modifier = Modifier
                                .size(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                        )
                    } else {
                        Icon(
                            Icons.Rounded.QrCode,
                            contentDescription = "No hay QR",
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!row.numero.isNullOrBlank()) {
                        Text(
                            text = stringResource(R.string.memb_contribs_dialog_number_label),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = row.numero,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(row.numero))
                                scope.launch { snackbar.showSnackbar(snackCopied) }
                            }) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = stringResource(R.string.memb_contribs_dialog_copy_cd))
                            }
                        }
                    }
                    Button(
                        onClick = onUploadClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.memb_contribs_dialog_upload_button))
                    }
                }
                SnackbarHost(
                    hostState = snackbar,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_close))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}


@Composable private fun Labeled(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(120.dp), style = MaterialTheme.typography.bodySmall)
        Text(value, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
    }
}

@Composable private fun StatusPill(statusKey: String) {
    val (bg, fg, text) = when (statusKey) {
        "PAGADO"      -> Triple(SuccessColor.copy(.15f), SuccessColor, stringResource(R.string.memb_contribs_status_paid))
        "EN_REVISION" -> Triple(InfoColor.copy(.15f),    InfoColor,    stringResource(R.string.memb_contribs_status_review))
        "RECHAZADO"   -> Triple(DangerColor.copy(.15f),  DangerColor,  stringResource(R.string.memb_contribs_status_rejected))
        else          -> Triple(WarningColor.copy(.15f), WarningColor, stringResource(R.string.memb_contribs_status_pending))
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable private fun HorizontalLine() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline)
    )
}

private fun multipartFromUri(context: Context, uri: Uri, partName: String = "file"): MultipartBody.Part? {
    val cr = context.contentResolver
    val name = queryDisplayName(cr, uri) ?: "receipt"
    val type = cr.getType(uri) ?: "application/octet-stream"
    val bytes = cr.openInputStream(uri)?.use { it.readBytes() } ?: return null
    val body = bytes.toRequestBody(type.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(partName, name, body)
}

private fun queryDisplayName(cr: android.content.ContentResolver, uri: Uri): String? {
    var result: String? = null
    var cursor: Cursor? = null
    try {
        cursor = cr.query(uri, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0) result = cursor.getString(idx)
        }
    } finally { cursor?.close() }
    return result
}