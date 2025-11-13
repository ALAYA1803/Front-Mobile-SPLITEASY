package com.spliteasy.spliteasy.ui.representative.contributions

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spliteasy.spliteasy.R
import com.spliteasy.spliteasy.data.remote.dto.PaymentReceiptDto
import com.spliteasy.spliteasy.ui.theme.DangerColor
import com.spliteasy.spliteasy.ui.theme.SuccessColor
import com.spliteasy.spliteasy.ui.theme.WarningColor
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import androidx.compose.material.icons.rounded.Info

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepContributionsScreen(
    vm: RepContributionsViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val formSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val reviewSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            vm.onQrImageSelected(uri)
        }
    )

    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.rep_contrib_title),
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val hh = if (ui.householdName.isNotBlank()) ui.householdName else stringResource(R.string.rep_contrib_vm_default_household)
                        val count = ui.contributions.size
                        val subtitle = if (count == 1) {
                            stringResource(R.string.rep_contrib_subtitle_single, hh, count)
                        } else {
                            stringResource(R.string.rep_contrib_subtitle_plural, hh, count)
                        }
                        Text(
                            subtitle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            if (ui.isRepresentante) {
                ExtendedFloatingActionButton(
                    onClick = {
                        vm.openForm()
                        scope.launch { formSheetState.show() }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.rep_contrib_fab)) },
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
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                ContributionsList(
                    items = ui.contributions,
                    onToggle = vm::toggleExpanded,
                    onDelete = vm::delete,
                    onOpenReview = {
                        vm.openReview(it)
                        scope.launch { reviewSheetState.show() }
                    },
                    currency = ui.currency
                )
            }
        }
    }

    if (ui.formVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch { formSheetState.hide() }.invokeOnCompletion { vm.closeForm() }
            },
            sheetState = formSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { SheetHandle() },
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
                },
                onNumeroChange = vm::onNumeroChange,
                onQrButtonClick = { imagePickerLauncher.launch("image/*") }
            )
        }
    }

    if (ui.reviewVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch { reviewSheetState.hide() }.invokeOnCompletion { vm.closeReview() }
            },
            sheetState = reviewSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { SheetHandle() },
        ) {
            ReviewReceiptsSheet(
                ui = ui,
                currency = ui.currency,
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


@Composable
private fun ContributionsList(
    items: List<ContributionUi>,
    onToggle: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onOpenReview: (ContributionDetailUi) -> Unit,
    currency: String
) {
    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.rep_contrib_list_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val formatter = remember(currency) { currencyFormatter(currency) }
    val fallbackDash = stringResource(R.string.common_fallback_dash)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items, key = { it.id }) { c ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    width = 1.dp,
                    brush = SolidColor(MaterialTheme.colorScheme.outline)
                )
            ) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                c.description ?: fallbackDash,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val date = c.fechaLimite ?: fallbackDash
                            Text(
                                stringResource(R.string.rep_contrib_card_due_date, date) +
                                        "  •  " +
                                        stringResource(R.string.rep_contrib_card_total, formatter.format(c.montoTotal)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { onToggle(c.id) }) {
                            Icon(
                                if (c.expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onDelete(c.id) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.rep_contrib_cd_delete), tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    if (c.expanded) {
                        Spacer(Modifier.height(8.dp))
                        ContributionDetailsList(
                            details = c.details,
                            onOpenReview = onOpenReview,
                            formatter = formatter
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContributionDetailsList(
    details: List<ContributionDetailUi>,
    onOpenReview: (ContributionDetailUi) -> Unit,
    formatter: NumberFormat
) {
    val fallbackInitial = stringResource(R.string.member_home_member_initial_fallback)
    val receiptsText = stringResource(R.string.rep_contrib_detail_receipts)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
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
                val initial = d.displayName.trim().ifBlank { fallbackInitial }.first().uppercaseChar().toString()
                Box(
                    Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(.18f)),
                    contentAlignment = Alignment.Center
                ) { Text(initial, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge) }

                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(d.displayName, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)

                    val roleText = if (d.displayRole == "REPRESENTANTE") {
                        stringResource(R.string.rep_contrib_form_role_rep)
                    } else {
                        stringResource(R.string.rep_contrib_form_role_member)
                    }
                    Text("$roleText • ${formatter.format(d.monto)}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }

                StatusChip(d.status)

                Spacer(Modifier.width(10.dp))
                AssistChip(
                    onClick = { onOpenReview(d) },
                    label = {
                        Text(
                            if (d.pendingReceiptsCount > 0) {
                                stringResource(R.string.rep_contrib_detail_receipts_pending, d.pendingReceiptsCount)
                            } else {
                                receiptsText
                            },
                            color = if (d.pendingReceiptsCount > 0) WarningColor else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (d.pendingReceiptsCount > 0) WarningColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

@Composable private fun StatusChip(statusKey: String) {
    val (bg, fg, label) = when (statusKey.uppercase()) {
        "PAGADO" -> Triple(SuccessColor.copy(.15f), SuccessColor, stringResource(R.string.rep_contrib_status_paid))
        "EN_REVISION" -> Triple(WarningColor.copy(.12f), WarningColor, stringResource(R.string.rep_contrib_status_review))
        "RECHAZADO" -> Triple(DangerColor.copy(.12f), DangerColor, stringResource(R.string.rep_contrib_status_rejected))
        else -> Triple(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurfaceVariant, stringResource(R.string.rep_contrib_status_pending))
    }
    AssistChip(
        onClick = {},
        label = { Text(label, color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        colors = AssistChipDefaults.assistChipColors(containerColor = bg, labelColor = fg)
    )
}

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
    onSave: () -> Unit,
    onNumeroChange: (String) -> Unit,
    onQrButtonClick: () -> Unit
) {
    var billsOpen by remember { mutableStateOf(false) }
    var datePickerOpen by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val fallbackInitial = stringResource(R.string.member_home_member_initial_fallback)

    var showHelpDialog by remember { mutableStateOf(false) }

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
                }) { Text(stringResource(R.string.common_ok)) }
            },
            dismissButton = { TextButton(onClick = { datePickerOpen = false }) { Text(stringResource(R.string.rep_contrib_form_button_cancel)) } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    if (showHelpDialog) {
        val isIncome = ui.formStrategy == "INCOME_BASED"
        val titleId = if (isIncome) R.string.rep_contrib_form_strategy_help_income_title else R.string.rep_contrib_form_strategy_help_equal_title
        val descId = if (isIncome) R.string.rep_contrib_form_strategy_help_income_desc else R.string.rep_contrib_form_strategy_help_equal_desc

        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(stringResource(R.string.rep_contrib_form_strategy_help_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(titleId),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        stringResource(descId),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text(stringResource(R.string.common_close))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }


    Column(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Text(
            stringResource(R.string.rep_contrib_form_title),
            color = MaterialTheme.colorScheme.onSurface,
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

            ExposedDropdownMenuBox(expanded = billsOpen, onExpandedChange = { billsOpen = it }) {
                OutlinedTextField(
                    value = ui.allBills.firstOrNull { it.id == ui.formBillId }?.description ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.rep_contrib_form_label_bill)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = billsOpen) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = fieldColors()
                )
                ExposedDropdownMenu(
                    expanded = billsOpen,
                    onDismissRequest = { billsOpen = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    ui.allBills.forEach { b ->
                        DropdownMenuItem(
                            text = { Text(b.description ?: stringResource(R.string.memb_contribs_fallback_bill) + " #${b.id}") },
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
                label = { Text(stringResource(R.string.rep_contrib_form_label_desc)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors()
            )

            OutlinedTextField(
                value = ui.formFechaLimite,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.rep_contrib_form_label_due_date)) },
                trailingIcon = {
                    IconButton(onClick = { datePickerOpen = true }) {
                        Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors()
            )

            OutlinedTextField(
                value = ui.formNumero,
                onValueChange = onNumeroChange,
                label = { Text(stringResource(R.string.rep_contrib_form_label_number)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onQrButtonClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.QrCode, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.rep_contrib_form_button_qr))
                }

                if (ui.formQrUri != null) {
                    Box(
                        Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = ui.formQrUri),
                            contentDescription = stringResource(R.string.rep_contrib_form_qr_preview_cd),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }


            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = ui.formStrategy == "EQUAL",
                    onClick = { onStrategy("EQUAL") },
                    label = { Text(stringResource(R.string.rep_contrib_form_strategy_equal)) }
                )
                FilterChip(
                    selected = ui.formStrategy == "INCOME_BASED",
                    onClick = { onStrategy("INCOME_BASED") },
                    label = { Text(stringResource(R.string.rep_contrib_form_strategy_income)) }
                )

                IconButton(onClick = { showHelpDialog = true }) {
                    Icon(
                        Icons.Rounded.Info,
                        contentDescription = stringResource(R.string.rep_contrib_form_strategy_help_cd),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(stringResource(R.string.rep_contrib_form_members_title), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                ui.allMembers.forEach { m ->
                    val selected = ui.formSelectedMembers.contains(m.memberId)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) MaterialTheme.colorScheme.primary.copy(.15f) else Color.Transparent)
                            .clickable { onToggleMember(m.memberId) }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val initial = m.username.trim().ifBlank { fallbackInitial }.first().uppercaseChar().toString()
                        Box(
                            Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(.18f)),
                            contentAlignment = Alignment.Center
                        ) { Text(initial, color = MaterialTheme.colorScheme.primary) }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(m.username, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            val roleText = if (m.isRepresentative) {
                                stringResource(R.string.rep_contrib_form_role_rep)
                            } else {
                                stringResource(R.string.rep_contrib_form_role_member)
                            }
                            Text(roleText, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                        Checkbox(checked = selected, onCheckedChange = { onToggleMember(m.memberId) })
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) { Text(stringResource(R.string.rep_contrib_form_button_cancel)) }
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text(stringResource(R.string.rep_contrib_form_button_save)) }
        }
    }
}

@Composable
private fun ReviewReceiptsSheet(
    ui: RepContribUi,
    currency: String,
    onClose: () -> Unit,
    onDownload: (PaymentReceiptDto) -> Unit,
    onApprove: (PaymentReceiptDto) -> Unit,
    onReject: (PaymentReceiptDto) -> Unit
) {
    val formatter = remember(currency) { currencyFormatter(currency) }

    Column(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Text(
            stringResource(R.string.rep_contrib_review_title),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        val d = ui.reviewForDetail
        if (d != null) {
            Text(
                "${d.displayName} • ${formatter.format(d.monto)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (ui.reviewLoading) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            if (ui.reviewReceipts.isEmpty()) {
                Text(stringResource(R.string.rep_contrib_review_empty), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ui.reviewReceipts, key = { it.id }) { r ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { onDownload(r) }) {
                                        Icon(Icons.Rounded.Download, contentDescription = stringResource(R.string.rep_contrib_review_cd_download), tint = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Text(r.filename, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(r.uploadedAt, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
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
                                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = SuccessColor.copy(.15f))
                                    ) { Text(stringResource(R.string.rep_contrib_review_button_approve), color = SuccessColor) }
                                    OutlinedButton(
                                        onClick = { onReject(r) },
                                        enabled = r.status.equals("PENDING", ignoreCase = true),
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerColor),
                                        border = BorderStroke(1.dp, DangerColor.copy(alpha = 0.5f))
                                    ) { Text(stringResource(R.string.rep_contrib_review_button_reject), color = DangerColor) }
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
            TextButton(onClick = onClose) { Text(stringResource(R.string.rep_contrib_review_button_close)) }
        }
    }
}


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
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
        )
    }
}

@Composable private fun ErrorBar(msg: String, onRetry: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.errorContainer) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(msg, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
            TextButton(onClick = onRetry) { Text(stringResource(R.string.rep_bills_error_retry), color = MaterialTheme.colorScheme.onErrorContainer) }
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
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
)

private fun formatPen(value: Double): String =
    NumberFormat.getCurrencyInstance(Locale("es", "PE")).apply {
        currency = Currency.getInstance("PEN")
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }.format(value)

private fun currencyFormatter(code: String): NumberFormat {
    val locale = if (code == "PEN") Locale("es", "PE") else Locale.getDefault()
    return NumberFormat.getCurrencyInstance(locale).apply {
        try { currency = Currency.getInstance(code) } catch (_: Throwable) {
            currency = Currency.getInstance("PEN")
        }
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }
}

private fun Context.downloadReceipt(url: String?, filename: String?) {
    if (url.isNullOrBlank()) return
    val fallbackName = this.getString(R.string.rep_contrib_receipt_fallback_name)
    val safeName = (filename?.ifBlank { null } ?: fallbackName).replace(Regex("[^a-zA-Z0-9._-]"), "_")

    val request = DownloadManager.Request(Uri.parse(url))
        .setTitle(safeName)
        .setDescription(this.getString(R.string.rep_contrib_receipt_download_title))
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeName)
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)

    val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    dm.enqueue(request)
}