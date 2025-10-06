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

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val mcId = pendingUploadFor
        pendingUploadFor = null
        if (uri != null && mcId != null) {
            multipartFromUri(ctx, uri)?.let { part ->
                vm.uploadReceipt(mcId, part) { ok ->
                    scope.launch {
                        snackbar.showSnackbar(if (ok) "Boleta enviada" else "No se pudo enviar la boleta")
                    }
                }
            } ?: scope.launch { snackbar.showSnackbar("No se pudo leer el archivo") }
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
                            "Contribuciones",
                            color = TextPri,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Sube el comprobante de pago o revisa el estado.",
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
                                Text("No hay contribuciones pendientes.", color = TextSec)
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
                    text = row.billDescription ?: "Factura",
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

            Labeled("Descripción", row.contribDescription ?: "—")
            Labeled("Estrategia", when(row.strategy?.uppercase()) {
                "EQUAL" -> "Partes iguales"
                "INCOME_BASED" -> "Según ingresos"
                else -> row.strategy ?: "—"
            })
            Labeled("Fecha factura", row.billDate ?: "—")
            Labeled("Fecha límite", row.dueDate ?: "—")
            Labeled("Monto facturado", row.billAmount?.let { "S/ %.2f".format(it) } ?: "—")
            Labeled("Monto a pagar", "S/ %.2f".format(row.mc.amount))

            Spacer(Modifier.height(10.dp))

            val canUpload = row.statusUi != "PAGADO"
            Button(
                onClick = onUpload,
                enabled = canUpload,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (row.statusUi == "EN_REVISION") InfoColor else BrandPrimary
                )
            ) {
                Text(if (row.statusUi == "EN_REVISION") "Reemplazar boleta" else "Pagar / Subir boleta")
            }
        }
    }
}

@Composable private fun Labeled(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextSec, modifier = Modifier.width(120.dp))
        Text(value, color = TextPri)
    }
}

@Composable private fun StatusPill(status: String) {
    val (bg, fg, text) = when (status) {
        "PAGADO"      -> Triple(SuccessColor.copy(.15f), SuccessColor, "Pagado")
        "EN_REVISION" -> Triple(InfoColor.copy(.15f),    InfoColor,    "En revisión")
        "RECHAZADO"   -> Triple(DangerColor.copy(.15f),  DangerColor,  "Rechazado")
        else          -> Triple(WarningColor.copy(.15f), WarningColor, "Pendiente")
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
