package com.spliteasy.spliteasy.ui.member.contribs

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import com.spliteasy.spliteasy.R

private val BrandPrimary = Color(0xFF1565C0)
private val InfoColor    = Color(0xFF1A73E8)
private val SuccessColor = Color(0xFF2E7D32)
private val WarningColor = Color(0xFFFF8F00)
private val DangerColor  = Color(0xFFD32F2F)
private val BgMain       = Color(0xFF1A1A1A)
private val BgCard       = Color(0xFF2D2D2D)
private val Border       = Color(0xFF404040)
private val TextPri      = Color(0xFFF8F9FA)
private val TextSec      = Color(0xFFADB5BD)

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

    var pendingUploadFor by remember { mutableStateOf<Long?>(null) }

    val snackSuccess = stringResource(R.string.memb_contribs_toast_upload_success)
    val snackFail = stringResource(R.string.memb_contribs_toast_upload_fail)
    val snackReadFail = stringResource(R.string.memb_contribs_toast_read_fail)

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val mcId = pendingUploadFor
        pendingUploadFor = null
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
        containerColor = BgMain,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        when (val s = ui) {
            is ContribsUiState.Loading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = BrandPrimary) }

            is ContribsUiState.Error -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { Text(s.message, color = TextSec) }

            is ContribsUiState.Ready -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgMain)
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            stringResource(R.string.memb_contribs_title),
                            color = TextPri,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.memb_contribs_subtitle),
                            color = TextSec,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    items(s.rows, key = { it.mc.id }) { row ->
                        ContribCard(
                            row = row,
                            onUpload = {
                                pendingUploadFor = row.mc.id
                                filePicker.launch("*/*")
                            }
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
                                Text(stringResource(R.string.memb_contribs_empty_list), color = TextSec)
                            }
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ContribCard(
    row: ContribRow,
    onUpload: () -> Unit
) {
    val fallbackDash = stringResource(R.string.common_fallback_dash)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BgCard,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = row.billDescription ?: stringResource(R.string.memb_contribs_fallback_bill),
                    color = TextPri,
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

            val canUpload = row.statusUi != "PAGADO"

            val buttonText = if (row.statusUi == "EN_REVISION") {
                stringResource(R.string.memb_contribs_button_replace)
            } else {
                stringResource(R.string.memb_contribs_button_upload)
            }

            Button(
                onClick = onUpload,
                enabled = canUpload,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (row.statusUi == "EN_REVISION") InfoColor else BrandPrimary
                )
            ) {
                Text(buttonText)
            }
        }
    }
}

@Composable private fun Labeled(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextSec, modifier = Modifier.width(120.dp), style = MaterialTheme.typography.bodySmall)
        Text(value, color = TextPri, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
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
            .background(Border)
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