package com.spliteasy.spliteasy.ui.representative.bills

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.spliteasy.spliteasy.R

private val BrandPrimary = Color(0xFF1565C0)
private val BrandPrimarySoft = Color(0x331565C0)
private val BgMain = Color(0xFF1A1A1A)
private val CardBg = Color(0xFF1B1E24)
private val CardBgElev = Color(0xFF222632)
private val Border = Color(0xFF2B2F3A)
private val TextPri = Color(0xFFF3F4F6)
private val TextSec = Color(0xFF9AA0A6)
private val Danger = Color(0xFFE53935)

@Composable
fun RepBillsScreen(
    vm: RepBillsViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    val currencyFormat = remember(ui.currency) {
        currencyFormatter(ui.currency)
    }
    val currencyDefault = stringResource(R.string.rep_bills_currency_default)

    val billsCount = ui.bills.size
    val billsInfoText = pluralStringResource(
        id = R.plurals.rep_bills_info_suffix_plural,
        count = billsCount,
        formatArgs = arrayOf(billsCount)
    )
    val rightInfo = if (ui.householdName.isNotBlank())
        "${ui.householdName} • $billsInfoText"
    else
        billsInfoText

    Surface(Modifier.fillMaxSize(), color = BgMain) {
        Box(Modifier.fillMaxSize()) {

            Column(Modifier.fillMaxSize()) {

                TopHeader(
                    title = stringResource(R.string.rep_bills_title),
                    subtitle = stringResource(R.string.rep_bills_subtitle),
                    rightInfo = rightInfo
                )

                if (ui.error != null) {
                    ErrorBar(ui.error!!) { vm.load() }
                }

                if (ui.loading) {
                    LoadingListSkeleton()
                } else {
                    BillsList(
                        bills = ui.bills,
                        isRepresentante = ui.isRepresentante,
                        onEdit = { vm.openForm(it) },
                        onDelete = { vm.delete(it.id) },
                        currencyFormat = currencyFormat,
                        currencyDefault = currencyDefault
                    )
                }
            }

            if (ui.isRepresentante) {
                ExtendedFloatingActionButton(
                    onClick = { vm.openForm(null) },
                    containerColor = BrandPrimary,
                    contentColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.rep_bills_fab)) }
                )
            }

            if (ui.formVisible) {
                BillDialog(
                    title = if (ui.editingId == null) {
                        stringResource(R.string.rep_bills_dialog_title_new)
                    } else {
                        stringResource(R.string.rep_bills_dialog_title_edit)
                    },
                    description = ui.formDescription,
                    amount = ui.formMonto,
                    date = ui.formFecha,
                    currency = ui.currency,
                    onDesc = vm::onDescChange,
                    onAmount = vm::onMontoChange,
                    onDate = vm::onFechaChange,
                    onCancel = vm::closeForm,
                    onSave = vm::submit
                )
            }
        }
    }
}


@Composable
private fun TopHeader(
    title: String,
    subtitle: String,
    rightInfo: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    listOf(BgMain, BgMain, CardBg.copy(alpha = .35f))
                )
            )
            .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    color = TextPri,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    color = TextSec,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        rightInfo,
                        color = TextPri,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    labelColor = TextPri,
                    containerColor = CardBg
                )
            )
        }
        Spacer(Modifier.height(10.dp))
        Divider(color = Border, thickness = 1.dp)
    }
}

@Composable
private fun ErrorBar(msg: String, onRetry: () -> Unit) {
    Surface(
        color = Danger.copy(alpha = 0.12f),
        contentColor = Color.White,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(msg, color = Color(0xFFFFCDD2), modifier = Modifier.weight(1f))
            TextButton(onClick = onRetry) { Text(stringResource(R.string.rep_bills_error_retry), color = Color.White) }
        }
    }
}

@Composable
private fun LoadingListSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(4) {
            Surface(
                color = CardBg,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Border)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                        .background(CardBg)
                        .padding(16.dp)
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(BrandPrimarySoft, RoundedCornerShape(16.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun BillsList(
    bills: List<BillUi>,
    isRepresentante: Boolean,
    onEdit: (BillUi) -> Unit,
    onDelete: (BillUi) -> Unit,
    currencyFormat: NumberFormat,
    currencyDefault: String
) {
    if (bills.isEmpty()) {
        EmptyState()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(bills) { b ->
            BillRowCard(
                bill = b,
                isRepresentante = isRepresentante,
                onEdit = { onEdit(b) },
                onDelete = { onDelete(b) },
                currencyFormat = currencyFormat,
                currencyDefault = currencyDefault
            )
        }
    }
}

@Composable
private fun BillRowCard(
    bill: BillUi,
    isRepresentante: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    currencyFormat: NumberFormat,
    currencyDefault: String
) {
    val fallbackDash = stringResource(R.string.common_fallback_dash)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.widthIn(min = 110.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = bill.monto?.let { currencyFormat.format(it) } ?: currencyDefault,
                    color = TextPri,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(Modifier.height(2.dp))
                AssistChip(
                    onClick = {},
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = TextSec)
                            Spacer(Modifier.width(6.dp))
                            Text(bill.fecha ?: fallbackDash, color = TextSec)
                        }
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = CardBgElev,
                        labelColor = TextSec
                    )
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    bill.description ?: fallbackDash,
                    color = TextPri,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(6.dp))


            }

            if (isRepresentante) {
                Spacer(Modifier.width(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedIconButton(
                        onClick = onEdit,
                        border = BorderStroke(1.dp, Border),
                        colors = IconButtonDefaults.outlinedIconButtonColors(
                            containerColor = CardBgElev, contentColor = TextSec
                        )
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.rep_bills_cd_edit))
                    }
                    OutlinedIconButton(
                        onClick = onDelete,
                        border = BorderStroke(1.dp, Border),
                        colors = IconButtonDefaults.outlinedIconButtonColors(
                            containerColor = CardBgElev, contentColor = Danger
                        )
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.rep_bills_cd_delete))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(CardBg),
            contentAlignment = Alignment.Center
        ) {
            Text("∑", color = BrandPrimary, style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.rep_bills_empty_title), color = TextPri, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(R.string.rep_bills_empty_subtitle),
            color = TextSec,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BillDialog(
    title: String,
    description: String,
    amount: String,
    date: String,
    currency: String,
    onDesc: (String) -> Unit,
    onAmount: (String) -> Unit,
    onDate: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
            ) { Text(stringResource(R.string.rep_bills_dialog_save)) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onCancel,
                border = BorderStroke(1.dp, Border),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPri)
            ) { Text(stringResource(R.string.rep_bills_dialog_cancel)) }
        },
        title = {
            Text(
                title,
                color = TextPri,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = onDesc,
                    label = { Text(stringResource(R.string.rep_bills_dialog_label_desc)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = Border,
                        focusedLabelColor = BrandPrimary,
                        unfocusedLabelColor = TextSec,
                        cursorColor = BrandPrimary,
                        focusedTextColor = TextPri,
                        unfocusedTextColor = TextPri
                    )
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmount,
                    label = { Text(stringResource(R.string.rep_bills_dialog_label_amount, currency)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = Border,
                        focusedLabelColor = BrandPrimary,
                        unfocusedLabelColor = TextSec,
                        cursorColor = BrandPrimary,
                        focusedTextColor = TextPri,
                        unfocusedTextColor = TextPri
                    )
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = onDate,
                    label = { Text(stringResource(R.string.rep_bills_dialog_label_date)) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = TextSec)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = Border,
                        focusedLabelColor = BrandPrimary,
                        unfocusedLabelColor = TextSec,
                        cursorColor = BrandPrimary,
                        focusedTextColor = TextPri,
                        unfocusedTextColor = TextPri
                    )
                )
            }
        },
        containerColor = CardBg,
        titleContentColor = TextPri,
        textContentColor = TextSec,
        shape = RoundedCornerShape(20.dp)
    )
}

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