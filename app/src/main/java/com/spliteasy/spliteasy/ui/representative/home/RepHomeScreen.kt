package com.spliteasy.spliteasy.ui.representative.home

import androidx.compose.animation.core.animateIntAsState
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
import java.util.Currency
import java.util.Locale
import androidx.compose.foundation.BorderStroke

private val BrandPrimary   = Color(0xFF1565C0)
private val BrandSecondary = Color(0xFFFF6F00)
private val SuccessColor   = Color(0xFF2E7D32)
private val InfoColor      = Color(0xFF4F46E5)

private val BgMain        = Color(0xFF1A1A1A)
private val CardBg        = Color(0xFF2D2D2D)
private val BorderColor   = Color(0xFF404040)
private val TextPrimary   = Color(0xFFF8F9FA)
private val TextSecondary = Color(0xFFADB5BD)

@Composable
fun RepHomeScreen(
    vm: RepHomeViewModel = hiltViewModel(),
    onCreateHousehold: () -> Unit = {}
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


@Composable
private fun Dashboard(ui: RepHomeUi) {
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
            Section(title = "Resumen rápido") {
                Column(
                    Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            title = "Miembros",
                            value = ui.membersCount,
                            icon = Icons.Rounded.Groups,
                            tint = BrandPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Facturas",
                            value = ui.billsCount,
                            icon = Icons.Rounded.ReceiptLong,
                            tint = BrandSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            title = "Contribuciones",
                            value = ui.contributionsCount,
                            icon = Icons.Rounded.Wallet,
                            tint = InfoColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun Hero(
    householdName: String,
    householdDesc: String
) {
    val gradient = Brush.verticalGradient(
        listOf(BrandPrimary.copy(alpha = .22f), Color.Transparent)
    )
    Surface(color = BgMain) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(top = 18.dp, bottom = 8.dp)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "Panel del hogar",
                color = TextSecondary,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (householdName.isBlank()) "—" else householdName,
                color = TextPrimary,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (householdDesc.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(householdDesc, color = TextSecondary)
            }

            // Pills de estado/moneda
            Spacer(Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    enabled = true,
                    label = { Text("Moneda: PEN") },
                    leadingIcon = { /* … */ },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = CardBg,
                        labelColor = TextPrimary
                    ),
                    border = BorderStroke(1.dp, BorderColor)
                )
                AssistChip(
                    onClick = {},
                    label = { Text("Activo") },
                    leadingIcon = {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = SuccessColor)
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = CardBg,
                        labelColor = TextPrimary
                    ),
                    border = BorderStroke(1.dp, BorderColor)
                )
            }
            Spacer(Modifier.height(8.dp))
            Divider(color = BorderColor)
        }
    }
}

@Composable
private fun QuickActionsRow(
    onMembers: (() -> Unit)? = null,
    onBills:   (() -> Unit)? = null,
    onContrib: (() -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionChip("Miembros", Icons.Rounded.Groups, BrandPrimary) { onMembers?.invoke() }
        ActionChip("Facturas", Icons.Rounded.ReceiptLong, BrandSecondary) { onBills?.invoke() }
        ActionChip("Aportes", Icons.Rounded.Wallet, InfoColor) { onContrib?.invoke() }
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun ActionChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    FilterChip(
        selected = false,
        onClick = onClick,
        enabled = true,
        label = { Text(text) },
        leadingIcon = { /* … */ },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = CardBg,
            labelColor = TextPrimary
        ),
        border = BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun Section(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            title,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = TextPrimary,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        content()
    }
}

@Composable
private fun StatCard(
    title: String,
    value: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val animated by animateIntAsState(targetValue = value, label = "stat")

    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = CardBg),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
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
                    .background(tint.copy(alpha = .18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelLarge.copy(color = TextSecondary))
                Text(
                    animated.toString(),
                    style = MaterialTheme.typography.headlineSmall.copy(
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
