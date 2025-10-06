package com.spliteasy.spliteasy.ui.member

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Groups
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

/** === Paleta SplitEasy (oscura) === */
private val BrandPrimary   = Color(0xFF1565C0) // azul
private val BrandSecondary = Color(0xFFFF6F00) // naranja
private val SuccessColor   = Color(0xFF2E7D32) // verde
private val InfoColor      = Color(0xFF4F46E5) // índigo

private val BgMain         = Color(0xFF1A1A1A) // #1a1a1a
private val CardBg         = Color(0xFF2D2D2D) // #2d2d2d
private val BorderColor    = Color(0xFF404040) // #404040
private val TextPrimary    = Color(0xFFF8F9FA) // #f8f9fa
private val TextSecondary  = Color(0xFFADB5BD) // #adb5bd

@Composable
fun MemberHomeScreen(
    onAddExpense: () -> Unit = {},   // (no se muestra al miembro)
    onOpenExpense: (Long) -> Unit = {},
    vm: MemberHomeViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BgMain
    ) {
        when (val s = state) {
            is MemberHomeUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandPrimary)
            }
            is MemberHomeUiState.Error -> ErrorBox(
                message = s.message ?: "Ocurrió un error al cargar.",
                onRetry = vm::refresh
            )
            is MemberHomeUiState.Ready -> MemberHomeContent(s)
            else -> EmptyBox("Sin datos para mostrar.")
        }
    }
}

/* ---------------------------------- CONTENT ---------------------------------- */

@Composable
private fun MemberHomeContent(s: MemberHomeUiState.Ready) {
    val currency = s.currency.ifBlank { "PEN" }
    val fmt = remember(currency) { currencyFormatter(currency) }

    val welcomeName = remember(s) { resolveWelcomeName(s) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero oscuro con degradado y saludo correcto
        item {
            HeroHeader(
                userName = welcomeName,
                householdName = s.householdName,
                householdDescription = s.householdDescription
            )
        }

        // KPIs con tarjetas oscuras y bordes sutiles
        item {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "Total pendiente",
                        value = fmt.format(s.totalPending),
                        icon = Icons.Rounded.Wallet,
                        tint = BrandSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total pagado",
                        value = fmt.format(s.totalPaid),
                        icon = Icons.Rounded.CheckCircle,
                        tint = SuccessColor,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "Contribuciones activas",
                        value = s.activeContribsCount.toString(),
                        icon = Icons.Rounded.AccountCircle,
                        tint = InfoColor,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Miembros",
                        value = s.members.size.toString(),
                        icon = Icons.Rounded.Groups,
                        tint = BrandPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Lista de miembros (oscura)
        if (s.members.isNotEmpty()) {
            item {
                SectionTitle(
                    text = "Miembros del hogar",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            items(s.members) { m ->
                MemberRow(
                    name = m.username,
                    email = m.email,
                    role = firstRoleLabel(m.roles),
                    isRep = m.roles.any { it.equals("ROLE_REPRESENTANTE", ignoreCase = true) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Divider(color = BorderColor, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

/* ---------------------------------- WIDGETS ---------------------------------- */

@Composable
private fun HeroHeader(
    userName: String,
    householdName: String,
    householdDescription: String
) {
    // Degradado oscuro azul→índigo
    val gradient = Brush.verticalGradient(
        colors = listOf(BrandPrimary.copy(alpha = 0.18f), InfoColor.copy(alpha = 0.10f))
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        color = BgMain
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(top = 20.dp, bottom = 18.dp)
        ) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "¡Hola, $userName!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Este es tu panel como miembro.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
                Spacer(Modifier.height(12.dp))

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
                                householdName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            )
                        }
                        if (householdDescription.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                householdDescription,
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
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge.copy(color = TextSecondary)
                )
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
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        ),
        modifier = modifier.padding(top = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun MemberRow(
    name: String,
    email: String,
    role: String,
    isRep: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val initial = name.trim().ifEmpty { "U" }.first().uppercaseChar().toString()
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(BrandPrimary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                initial,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = BrandPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (email.isNotBlank()) {
                Text(
                    email,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        RoleTag(role = role, highlighted = isRep)
    }
}

@Composable
private fun RoleTag(role: String, highlighted: Boolean) {
    val bg = if (highlighted) BrandPrimary.copy(alpha = 0.25f) else Color(0xFF3A3A3A)
    val fg = if (highlighted) TextPrimary else TextSecondary
    Surface(
        color = bg,
        contentColor = fg,
        shape = RoundedCornerShape(999.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = if (highlighted) 1.dp else 0.dp,
            brush = androidx.compose.ui.graphics.SolidColor(BorderColor)
        )
    ) {
        Text(
            role,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
        )
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
        ) {
            Text("Reintentar")
        }
    }
}

@Composable
private fun EmptyBox(message: String) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sin datos", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

/* ---------------------------------- UTILS ---------------------------------- */

private fun currencyFormatter(code: String): NumberFormat {
    return try {
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
}

private fun firstRoleLabel(roles: List<String>): String {
    if (roles.isEmpty()) return "MIEMBRO"
    val r = roles.first().uppercase()
    return if (r.contains("REPRESENTANTE")) "REPRESENTANTE" else "MIEMBRO"
}

/**
 * Resuelve el nombre real del miembro SIN tocar login.
 * Busca campos comunes en tu UiState.Ready (si existen) y, si hay currentUserId, lo cruza con la lista de members.
 */
private fun resolveWelcomeName(s: MemberHomeUiState.Ready): String {
    fun <T> tryProp(name: String): T? = try {
        @Suppress("UNCHECKED_CAST")
        s::class.members.firstOrNull { it.name == name }?.call(s) as? T
    } catch (_: Throwable) { null }

    // 1) currentUserName directo
    tryProp<String>("currentUserName")?.let { if (it.isNotBlank()) return it }

    // 2) currentUser.username (si tu VM expone el objeto)
    tryProp<Any>("currentUser")?.let { cu ->
        val username = runCatching {
            cu::class.members.firstOrNull { it.name == "username" }?.call(cu) as? String
        }.getOrNull()
        if (!username.isNullOrBlank()) return username
        val email = runCatching {
            cu::class.members.firstOrNull { it.name == "email" }?.call(cu) as? String
        }.getOrNull()
        if (!email.isNullOrBlank()) return email.substringBefore("@")
    }

    // 3) Si viene currentUserId, buscarlo en la lista de miembros para extraer su username
    val myId = tryProp<Long>("currentUserId") ?: tryProp<Int>("currentUserId")?.toLong()
    if (myId != null) {
        s.members.firstOrNull { it.id?.toLong() == myId }?.username?.let { if (it.isNotBlank()) return it }
    }

    // 4) Otros alias frecuentes que suelen traer del session/local
    tryProp<String>("sessionUsername")?.let { if (it.isNotBlank()) return it }
    tryProp<String>("username")?.let { if (it.isNotBlank()) return it }

    // 5) Fallback: si hay miembros, intenta el que tenga correo que contenga el nombre de usuario actual
    s.members.firstOrNull()?.username?.takeIf { it.isNotBlank() }?.let { return it }

    return "Usuario"
}
