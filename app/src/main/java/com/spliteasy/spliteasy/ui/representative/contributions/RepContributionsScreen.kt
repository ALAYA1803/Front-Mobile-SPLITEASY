package com.spliteasy.spliteasy.ui.representative.contributions

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spliteasy.spliteasy.data.remote.dto.PaymentReceiptDto
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar // o TopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog

/* =================
   Design tokens
   ================= */
private val Brand = Color(0xFF1565C0)
private val BgMain = Color(0xFF1A1A1A)
private val CardBg = Color(0xFF1B1E24)
private val CardBg2 = Color(0xFF222632)
private val Border = Color(0xFF2B2F3A)
private val TextPri = Color(0xFFF3F4F6)
private val TextSec = Color(0xFF9AA0A6)
private val Success = Color(0xFF2E7D32)
private val Warning = Color(0xFFFFB300)
private val Danger = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepContributionsScreen(
    vm: RepContributionsViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Bottom sheets
    val formSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val reviewSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) { vm.load() }

    // ---------- Top-level scaffold ----------
    Scaffold(
        containerColor = BgMain,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Contribuciones",
                            color = TextPri,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val hh = if (ui.householdName.isNotBlank()) ui.householdName else "Mi hogar"
                        val count = ui.contributions.size
                        Text(
                            "$hh • $count ${if (count == 1) "contribución" else "contribuciones"}",
                            color = TextSec,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgMain)
            )
        },
        floatingActionButton = {
            if (ui.isRepresentante) {
                ExtendedFloatingActionButton(
                    onClick = {
                        vm.openForm()
                        scope.launch { formSheetState.show() }
                    },
                    containerColor = Brand,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = { Text("Nueva contribución") },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            if (ui.error != null) {
                ErrorBar(msg = ui.error!!, onRetry = vm::load)
            }

            if (ui.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Brand)
                }
            } else {
                ContributionsList(
                    items = ui.contributions,
                    onToggle = vm::toggleExpanded,
                    onDelete = vm::delete,
                    onOpenReview = {
                        vm.openReview(it)
                        scope.launch { reviewSheetState.show() }
                    }
                )
            }
        }
    }

    // ---------- Bottom Sheet: Formulario ----------
    if (ui.formVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch { formSheetState.hide() }.invokeOnCompletion { vm.closeForm() }
            },
            sheetState = formSheetState,
            containerColor = CardBg,
            dragHandle = { SheetHandle() },
            // nos encargamos con paddings
        ) {
            CreateContributionSheet(
                ui = ui,
                onBill = vm::onBill,
                onDesc = vm::onDesc,
                onDate = vm::onDate,
                onStrategy = vm::onStrategy,
                onToggleMember = vm::toggleMember,
                onCancel = {
                    scope.launch { formSheetState.hide() }.invokeOnCompletion { vm.closeForm() }
                },
                onSave = {
                    vm.submit()
                }
            )
        }
    }

    // ---------- Bottom Sheet: Revisar boletas ----------
    if (ui.reviewVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch { reviewSheetState.hide() }.invokeOnCompletion { vm.closeReview() }
            },
            sheetState = reviewSheetState,
            containerColor = CardBg,
            dragHandle = { SheetHandle() },

        ) {
            ReviewReceiptsSheet(
                ui = ui,
                onClose = {
                    scope.launch { reviewSheetState.hide() }.invokeOnCompletion { vm.closeReview() }
                },
                onDownload = { r -> ctx.downloadReceipt(r.url, r.filename) },
                onApprove = { r -> vm.approveReceiptAndRefresh(r.id) },
                onReject = { r -> vm.rejectReceiptAndRefresh(r.id, notes = null) }
            )
        }
    }
}

/* ===================================
   LISTA PRINCIPAL (optimizada móvil)
   =================================== */

@Composable
private fun ContributionsList(
    items: List<ContributionUi>,
    onToggle: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onOpenReview: (ContributionDetailUi) -> Unit
) {
    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay contribuciones.", color = TextSec)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items, key = { it.id }) { c ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CardBg,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(Border)
                )
            ) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.ReceiptLong, contentDescription = null, tint = Brand)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                c.description ?: "—",
                                color = TextPri,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val date = c.fechaLimite ?: "—"
                            Text(
                                "Vence: $date  •  Total: ${formatPen(c.montoTotal)}",
                                color = TextSec,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { onToggle(c.id) }) {
                            Icon(
                                if (c.expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                tint = TextSec
                            )
                        }
                        IconButton(onClick = { onDelete(c.id) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Eliminar", tint = Danger)
                        }
                    }

                    if (c.expanded) {
                        Spacer(Modifier.height(8.dp))
                        ContributionDetailsList(details = c.details, onOpenReview = onOpenReview)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContributionDetailsList(
    details: List<ContributionDetailUi>,
    onOpenReview: (ContributionDetailUi) -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg2)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        details.forEach { d ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val initial = d.displayName.trim().ifBlank { "U" }.first().uppercaseChar().toString()
                Box(
                    Modifier.size(36.dp).clip(CircleShape).background(Brand.copy(.18f)),
                    contentAlignment = Alignment.Center
                ) { Text(initial, color = Brand, style = MaterialTheme.typography.labelLarge) }

                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(d.displayName, color = TextPri, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${d.displayRole} • ${formatPen(d.monto)}", color = TextSec, style = MaterialTheme.typography.bodySmall)
                }

                StatusChip(d.status)

                Spacer(Modifier.width(10.dp))
                AssistChip(
                    onClick = { onOpenReview(d) },
                    label = {
                        Text(
                            if (d.pendingReceiptsCount > 0) "Boletas (${d.pendingReceiptsCount})" else "Boletas",
                            color = TextPri,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (d.pendingReceiptsCount > 0) Color(0x33FFB300) else Color(0x332B2F3A),
                        labelColor = TextPri
                    )
                )
            }
        }
    }
}

@Composable private fun StatusChip(status: String) {
    val (bg, fg, label) = when (status.uppercase()) {
        "PAGADO" -> Triple(Success.copy(.15f), Success, "Pagado")
        "EN_REVISION" -> Triple(Warning.copy(.12f), Warning, "En revisión")
        "RECHAZADO" -> Triple(Danger.copy(.12f), Danger, "Rechazado")
        else -> Triple(Color(0x335E6A7D), TextSec, "Pendiente")
    }
    AssistChip(
        onClick = {},
        label = { Text(label, color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        colors = AssistChipDefaults.assistChipColors(containerColor = bg, labelColor = fg)
    )
}

/* =========================================
   SHEET: Crear contribución (mobile-first)
   ========================================= */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateContributionSheet(
    ui: RepContribUi,
    onBill: (Long?) -> Unit,
    onDesc: (String) -> Unit,
    onDate: (String) -> Unit,
    onStrategy: (String) -> Unit,
    onToggleMember: (Long) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    var billsOpen by remember { mutableStateOf(false) }
    var datePickerOpen by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (datePickerOpen) {
        DatePickerDialog(
            onDismissRequest = { datePickerOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val ymd = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                            .let { "%04d-%02d-%02d".format(it.year, it.monthValue, it.dayOfMonth) }
                        onDate(ymd)
                    }
                    datePickerOpen = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { datePickerOpen = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Text(
            "Nueva contribución",
            color = TextPri,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Bill dropdown
            ExposedDropdownMenuBox(expanded = billsOpen, onExpandedChange = { billsOpen = it }) {
                OutlinedTextField(
                    value = ui.allBills.firstOrNull { it.id == ui.formBillId }?.description ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Comprobante (Bill)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = billsOpen) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = billsOpen, onDismissRequest = { billsOpen = false }) {
                    ui.allBills.forEach { b ->
                        DropdownMenuItem(
                            text = { Text(b.description ?: "Bill #${b.id}") },
                            onClick = {
                                onBill(b.id)
                                billsOpen = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = ui.formDescription,
                onValueChange = onDesc,
                label = { Text("Descripción") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ui.formFechaLimite,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha límite") },
                trailingIcon = {
                    IconButton(onClick = { datePickerOpen = true }) {
                        Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = TextSec)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = ui.formStrategy == "EQUAL",
                    onClick = { onStrategy("EQUAL") },
                    label = { Text("Igualitaria") }
                )
                FilterChip(
                    selected = ui.formStrategy == "INCOME_BASED",
                    onClick = { onStrategy("INCOME_BASED") },
                    label = { Text("Según ingresos") }
                )
            }

            // Miembros
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBg2)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Miembros (toque para seleccionar)", color = TextSec, style = MaterialTheme.typography.bodySmall)
                ui.allMembers.forEach { m ->
                    val selected = ui.formSelectedMembers.contains(m.memberId)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) Brand.copy(.15f) else Color.Transparent)
                            .clickable { onToggleMember(m.memberId) }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val initial = m.username.trim().ifBlank { "U" }.first().uppercaseChar().toString()
                        Box(
                            Modifier.size(28.dp).clip(CircleShape).background(Brand.copy(.18f)),
                            contentAlignment = Alignment.Center
                        ) { Text(initial, color = Brand) }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(m.username, color = TextPri, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(if (m.isRepresentative) "REPRESENTANTE" else "MIEMBRO", color = TextSec, style = MaterialTheme.typography.bodySmall)
                        }
                        Checkbox(checked = selected, onCheckedChange = { onToggleMember(m.memberId) })
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        // Acciones pegadas abajo
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) { Text("Cancelar") }
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Brand)
            ) { Text("Guardar") }
        }
    }
}

/* ============================================
   SHEET: Revisar boletas (mobile-first)
   ============================================ */
@Composable
private fun ReviewReceiptsSheet(
    ui: RepContribUi,
    onClose: () -> Unit,
    onDownload: (PaymentReceiptDto) -> Unit,
    onApprove: (PaymentReceiptDto) -> Unit,
    onReject: (PaymentReceiptDto) -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Text(
            "Revisar boletas",
            color = TextPri,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        val d = ui.reviewForDetail
        if (d != null) {
            Text(
                "${d.displayName} • ${formatPen(d.monto)}",
                color = TextSec,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (ui.reviewLoading) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Brand)
            }
        } else {
            if (ui.reviewReceipts.isEmpty()) {
                Text("No hay boletas para este detalle.", color = TextSec, modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ui.reviewReceipts, key = { it.id }) { r ->
                        Surface(
                            color = CardBg2,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { onDownload(r) }) {
                                        Icon(Icons.Rounded.Download, contentDescription = "Descargar", tint = TextPri)
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Text(r.filename, color = TextPri, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(r.uploadedAt, color = TextSec, style = MaterialTheme.typography.bodySmall)
                                    }
                                    val status = r.status.uppercase()
                                    StatusChip(
                                        when (status) {
                                            "APPROVED" -> "PAGADO"
                                            "REJECTED" -> "RECHAZADO"
                                            else -> "EN_REVISION"
                                        }
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilledTonalButton(
                                        onClick = { onApprove(r) },
                                        enabled = r.status.equals("PENDING", ignoreCase = true),
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Success.copy(.15f))
                                    ) { Text("Aprobar", color = Success) }
                                    OutlinedButton(
                                        onClick = { onReject(r) },
                                        enabled = r.status.equals("PENDING", ignoreCase = true),
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Rechazar", color = Danger) }
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onClose) { Text("Cerrar") }
        }
    }
}

/* ======= UI helpers ======= */

@Composable
private fun SheetHandle() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(100))
                .background(Border)
        )
    }
}

@Composable private fun ErrorBar(msg: String, onRetry: () -> Unit) {
    Surface(color = Danger.copy(.12f)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(msg, color = Color(0xFFFFCDD2), modifier = Modifier.weight(1f))
            TextButton(onClick = onRetry) { Text("Reintentar", color = Color.White) }
        }
    }
}

/* --- Utils --- */
private fun formatPen(value: Double): String =
    NumberFormat.getCurrencyInstance(Locale("es", "PE")).apply {
        currency = Currency.getInstance("PEN")
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }.format(value)

/* ---- Descarga de boletas con DownloadManager ---- */
private fun Context.downloadReceipt(url: String?, filename: String?) {
    if (url.isNullOrBlank()) return
    val safeName = (filename?.ifBlank { null } ?: "receipt").replace(Regex("[^a-zA-Z0-9._-]"), "_")
    val request = DownloadManager.Request(Uri.parse(url))
        .setTitle(safeName)
        .setDescription("Descargando boleta")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeName)
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)

    val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    dm.enqueue(request)
}
