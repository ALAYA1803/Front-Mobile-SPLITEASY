@file:Suppress("FunctionName")

package com.spliteasy.spliteasy.ui.member.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spliteasy.spliteasy.R
import com.spliteasy.spliteasy.ui.theme.SuccessColor
import com.spliteasy.spliteasy.ui.theme.WarningColor
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
fun MembStatusScreen(vm: MembStatusViewModel = hiltViewModel()) {
    val state by vm.ui.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val s = state) {
            is StatusUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            is StatusUiState.Error -> StatusMessage(
                title = stringResource(R.string.common_empty_title),
                message = s.message
            )
            is StatusUiState.Empty -> StatusMessage(
                title = stringResource(R.string.common_empty_title),
                message = s.reason
            )
            is StatusUiState.Ready -> StatusList(rows = s.rows)
        }
    }
}

@Composable
private fun StatusList(rows: List<StatusRowUi>) {
    val format = remember { currencyFormatter("PEN") }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                stringResource(R.string.memb_status_title),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.memb_status_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        items(rows) { r ->
            StatusCard(row = r, amountText = format.format(r.monto))
        }

        if (rows.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.memb_status_empty_list),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusCard(row: StatusRowUi, amountText: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val (badgeBg, badgeFg, badgeText) = when (row.statusUi) {
                    "PAGADO"      -> Triple(SuccessColor.copy(alpha = .15f), SuccessColor, stringResource(R.string.memb_status_status_paid))
                    "EN_REVISION" -> Triple(WarningColor.copy(alpha = .15f), WarningColor, stringResource(R.string.memb_status_status_review))
                    else          -> Triple(MaterialTheme.colorScheme.primary.copy(alpha = .15f), MaterialTheme.colorScheme.primary, stringResource(R.string.memb_status_status_pending))
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(badgeText, color = badgeFg, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = row.descripcionFactura ?: stringResource(R.string.memb_status_fallback_bill_desc),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))
                Text(
                    text = amountText,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            Spacer(Modifier.height(8.dp))

            val fallbackDash = stringResource(R.string.common_fallback_dash)

            InfoRow(stringResource(R.string.memb_status_label_description), row.descripcionContrib ?: fallbackDash)
            InfoRow(stringResource(R.string.memb_status_label_strategy), strategyLabel(row.strategy))
            InfoRow(stringResource(R.string.memb_status_label_bill_date), row.fechaFactura?.let { friendlyDate(it) } ?: fallbackDash)
            InfoRow(stringResource(R.string.memb_status_label_due_date), row.fechaLimite?.let { friendlyDate(it) } ?: fallbackDash)
            InfoRow(stringResource(R.string.memb_status_label_paid_at), row.pagadoEn?.let { friendlyDateTime(it) } ?: fallbackDash)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(110.dp))
        Spacer(Modifier.width(8.dp))
        Text(value, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun StatusMessage(title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun currencyFormatter(code: String): NumberFormat =
    NumberFormat.getCurrencyInstance(Locale("es", "PE")).apply {
        try { currency = Currency.getInstance(code) } catch (_: Throwable) {}
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

@Composable
private fun strategyLabel(s: String?): String =
    when (s?.uppercase()) {
        "EQUAL"         -> stringResource(R.string.memb_status_strategy_equal)
        "INCOME_BASED"  -> stringResource(R.string.memb_status_strategy_income)
        else            -> s ?: stringResource(R.string.common_fallback_dash)
    }

private fun friendlyDate(input: String): String =
    input.take(10).let { ymd ->
        val p = ymd.split("-")
        if (p.size == 3) "${p[2]}/${p[1]}/${p[0]}" else input
    }

private fun friendlyDateTime(input: String): String {
    val d = friendlyDate(input)
    val time = input.drop(11).take(5)
    return if (time.length == 5) "$d $time" else d
}