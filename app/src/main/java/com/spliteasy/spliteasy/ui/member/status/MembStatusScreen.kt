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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/* Paleta oscura SpliteEasy (local para evitar dependencias a otras files) */
private val BrandPrimary   = Color(0xFF1565C0)
private val SuccessColor   = Color(0xFF2E7D32)
private val WarningColor   = Color(0xFFFF8F00)
private val BgMain         = Color(0xFF1A1A1A)
private val BgCard         = Color(0xFF2D2D2D)
private val Border         = Color(0xFF404040)
private val TextPri        = Color(0xFFF8F9FA)
private val TextSec        = Color(0xFFADB5BD)

@Composable
fun MembStatusScreen(vm: MembStatusViewModel = hiltViewModel()) {
    val state by vm.ui.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BgMain
    ) {
        when (val s = state) {
            is StatusUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandPrimary)
            }
            is StatusUiState.Error -> StatusMessage(
                title = "Error",
                message = s.message
            )
            is StatusUiState.Empty -> StatusMessage(
                title = "Sin datos",
                message = s.reason
            )
            is StatusUiState.Ready -> StatusList(rows = s.rows)
        }
    }
}

@Composable
private fun StatusList(rows: List<StatusRowUi>) {
    val format = remember { currencyFormatter("PEN") } // igual que tu web (S/)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgMain)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Contribuciones Pagadas",
                color = TextPri,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Historial de pagos realizados",
                color = TextSec,
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
                        "No hay pagos registrados.",
                        color = TextSec,
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
        color = BgCard,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            // Top line: factura + monto
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Insignia del estado
                val (badgeBg, badgeFg, badgeText) = when (row.statusUi) {
                    "PAGADO"      -> Triple(SuccessColor.copy(alpha = .15f), SuccessColor, "Pagado")
                    "EN_REVISION" -> Triple(WarningColor.copy(alpha = .15f), WarningColor, "En revisión")
                    else          -> Triple(BrandPrimary.copy(alpha = .15f), BrandPrimary, "Pendiente")
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
                    text = row.descripcionFactura ?: "Factura",
                    color = TextPri,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))
                Text(
                    text = amountText,
                    color = TextPri,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(Modifier.height(8.dp))
            Divider(color = Border, thickness = 1.dp)
            Spacer(Modifier.height(8.dp))

            // Detalles
            InfoRow("Descripción", row.descripcionContrib ?: "—")
            InfoRow("Estrategia", strategyLabel(row.strategy))
            InfoRow("Fecha factura", row.fechaFactura?.let { friendlyDate(it) } ?: "—")
            InfoRow("Fecha límite", row.fechaLimite?.let { friendlyDate(it) } ?: "—")
            InfoRow("Pagado en", row.pagadoEn?.let { friendlyDateTime(it) } ?: "—")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextSec, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(110.dp))
        Spacer(Modifier.width(8.dp))
        Text(value, color = TextPri, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun StatusMessage(title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgMain)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = TextPri, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(message, color = TextSec, style = MaterialTheme.typography.bodyMedium)
    }
}

/* -------------------- helpers de formato (simple/robusto) -------------------- */

private fun currencyFormatter(code: String): NumberFormat =
    NumberFormat.getCurrencyInstance(Locale("es", "PE")).apply {
        try { currency = Currency.getInstance(code) } catch (_: Throwable) {}
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

private fun strategyLabel(s: String?): String =
    when (s?.uppercase()) {
        "EQUAL"         -> "Partes iguales"
        "INCOME_BASED"  -> "Según ingresos"
        else            -> s ?: "—"
    }

/** Recibe "2025-01-31" o "2025-01-31T00:00:00Z" -> "31/01/2025" */
private fun friendlyDate(input: String): String =
    input.take(10).let { ymd ->
        val p = ymd.split("-")
        if (p.size == 3) "${p[2]}/${p[1]}/${p[0]}" else input
    }

/** Recibe "2025-01-31T14:25:00" -> "31/01/2025 14:25" (simple) */
private fun friendlyDateTime(input: String): String {
    val d = friendlyDate(input)
    val time = input.drop(11).take(5)
    return if (time.length == 5) "$d $time" else d
}
