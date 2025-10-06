package com.spliteasy.spliteasy.ui.representative.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.util.*

/* === Paleta coherente con MemberHomeScreen === */
private val BrandPrimary   = Color(0xFF1565C0) // azul
private val BrandSecondary = Color(0xFFFF6F00) // naranja
private val SuccessColor   = Color(0xFF2E7D32) // verde
private val InfoColor      = Color(0xFF4F46E5) // índigo

private val BgMain        = Color(0xFF1A1A1A)
private val CardBg        = Color(0xFF2D2D2D)
private val BorderColor   = Color(0xFF404040)
private val TextPrimary   = Color(0xFFF8F9FA)
private val TextSecondary = Color(0xFFADB5BD)

@Composable
fun RepHomeScreen(
    vm: RepHomeViewModel = hiltViewModel(),
    onCreateHousehold: () -> Unit = {} // <- conéctalo a tu flujo de creación/navegación
) {
    val ui by vm.ui.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    Surface(Modifier.fillMaxSize(), color = BgMain) {
        when {
            ui.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandPrimary)
            }
            ui.error != null -> ErrorBox(
                message = ui.error ?: "Ocurrió un error al cargar.",
                onRetry = vm::load
            )
            ui.showOnboarding -> OnboardingCard(onCreate = onCreateHousehold)
            else -> Dashboard(ui = ui)
        }
    }
}

/* ----------------------------- ONBOARDING ----------------------------- */

@Composable
private fun OnboardingCard(onCreate: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = CardBg,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.SolidColor(BorderColor)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    "¡Bienvenido 👋!",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "Aún no has creado tu hogar. Crea uno para comenzar.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = onCreate,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) { Text("Crear hogar") }
            }
        }
    }
}

/* ------------------------------ DASHBOARD ----------------------------- */

@Composable
private fun Dashboard(ui: RepHomeUi) {
    val currencyCode = ui.currency.ifBlank { "PEN" }
    val currencyFmt = remember(currencyCode) { currencyFormatter(currencyCode) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Hero(
                householdName = ui.household?.name.orEmpty(),
                householdDesc = ui.household?.description.orEmpty()
            )
        }

        item {
            Column(
                Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "Miembros",
                        value = ui.membersCount.toString(),
                        icon = Icons.Rounded.Groups,
                        tint = BrandPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Facturas",
                        value = ui.billsCount.toString(),
                        icon = Icons.Rounded.ReceiptLong,
                        tint = BrandSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "Contribuciones",
                        value = ui.contributionsCount.toString(),
                        icon = Icons.Rounded.Wallet,
                        tint = InfoColor,
                        modifier = Modifier.weight(1f)
                    )

                }
            }
        }
    }
}

/* ------------------------------ WIDGETS ------------------------------- */

@Composable
private fun Hero(
    householdName: String,
    householdDesc: String
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(BrandPrimary.copy(alpha = 0.18f), InfoColor.copy(alpha = 0.10f))
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BgMain,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(top = 20.dp, bottom = 18.dp)
        ) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "Resumen",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CardBg,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(BorderColor)
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AccountTree,
                                contentDescription = null,
                                tint = BrandPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (householdName.isBlank()) "—" else householdName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (householdDesc.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                householdDesc,
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(BorderColor)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(tint),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelLarge.copy(color = TextSecondary))
                Text(
                    value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }
        }
    }
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
        Text("Ups…", style = MaterialTheme.typography.titleLarge, color = Color(0xFFFF4D4F))
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
        ) { Text("Reintentar") }
    }
}

/* ------------------------------ UTILS --------------------------------- */

private fun currencyFormatter(code: String): NumberFormat =
    try {
        NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance(code)
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }
    } catch (_: Throwable) {
        NumberFormat.getCurrencyInstance(Locale("es", "PE")).apply {
            currency = Currency.getInstance("PEN")
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }
    }
