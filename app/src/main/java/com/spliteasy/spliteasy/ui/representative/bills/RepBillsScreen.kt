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

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
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
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = .35f)
                    )
                )
            )
            .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        rightInfo,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
        Spacer(Modifier.height(10.dp))
        Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
    }
}

@Composable
private fun ErrorBar(msg: String, onRetry: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(msg, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
            TextButton(onClick = onRetry) { Text(stringResource(R.string.rep_bills_error_retry), color = MaterialTheme.colorScheme.onErrorContainer) }
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
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(16.dp)
                            )
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
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(Modifier.height(2.dp))
                AssistChip(
                    onClick = {},
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(6.dp))
                            Text(bill.fecha ?: fallbackDash, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    bill.description ?: fallbackDash,
                    color = MaterialTheme.colorScheme.onSurface,
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
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = IconButtonDefaults.outlinedIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.rep_bills_cd_edit))
                    }
                    OutlinedIconButton(
                        onClick = onDelete,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = IconButtonDefaults.outlinedIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.error
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
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Text("∑", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.rep_bills_empty_title), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(R.string.rep_bills_empty_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text(stringResource(R.string.rep_bills_dialog_save)) }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onCancel,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) { Text(stringResource(R.string.rep_bills_dialog_cancel)) }
        },
        title = {
            Text(
                title,
                color = MaterialTheme.colorScheme.onSurface,
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
                    colors = fieldColors()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmount,
                    label = { Text(stringResource(R.string.rep_bills_dialog_label_amount, currency)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = fieldColors()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = onDate,
                    label = { Text(stringResource(R.string.rep_bills_dialog_label_date)) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    colors = fieldColors()
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(20.dp)
    )
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