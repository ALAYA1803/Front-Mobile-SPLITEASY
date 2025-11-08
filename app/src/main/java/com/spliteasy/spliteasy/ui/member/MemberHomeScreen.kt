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
import androidx.compose.ui.res.stringResource
import com.spliteasy.spliteasy.R


private val BrandPrimary   = Color(0xFF1565C0)
private val BrandSecondary = Color(0xFFFF6F00)
private val SuccessColor   = Color(0xFF2E7D32)
private val InfoColor      = Color(0xFF4F46E5)

private val BgMain         = Color(0xFF1A1A1A)
private val CardBg         = Color(0xFF2D2D2D)
private val BorderColor    = Color(0xFF404040)
private val TextPrimary    = Color(0xFFF8F9FA)
private val TextSecondary  = Color(0xFFADB5BD)

@Composable
fun MemberHomeScreen(
    onAddExpense: () -> Unit = {},
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
                message = s.message ?: stringResource(R.string.member_home_error_generic),
                onRetry = vm::refresh
            )
            is MemberHomeUiState.Ready -> MemberHomeContent(s)
            else -> EmptyBox(stringResource(R.string.member_home_empty_generic))
        }
    }
}


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
        item {
            HeroHeader(
                userName = welcomeName,
                householdName = s.householdName,
                householdDescription = s.householdDescription
            )
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = stringResource(R.string.member_home_stat_pending),
                        value = fmt.format(s.totalPending),
                        icon = Icons.Rounded.Wallet,
                        tint = BrandSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = stringResource(R.string.member_home_stat_paid),
                        value = fmt.format(s.totalPaid),
                        icon = Icons.Rounded.CheckCircle,
                        tint = SuccessColor,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = stringResource(R.string.member_home_stat_active_contribs),
                        value = s.activeContribsCount.toString(),
                        icon = Icons.Rounded.AccountCircle,
                        tint = InfoColor,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = stringResource(R.string.member_home_stat_members),
                        value = s.members.size.toString(),
                        icon = Icons.Rounded.Groups,
                        tint = BrandPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        if (s.members.isNotEmpty()) {
            item {
                SectionTitle(
                    text = stringResource(R.string.member_home_section_members),
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


@Composable
private fun HeroHeader(
    userName: String,
    householdName: String,
    householdDescription: String
) {
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
                    text = stringResource(R.string.member_home_hero_greeting, userName),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.member_home_hero_subtitle),
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
                Spacer(Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CardBg,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    border = ButtonDefaults.outlinedButtonBorder(
                        enabled = true
                    ).copy(
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
        border = ButtonDefaults.outlinedButtonBorder(
            enabled = true
        ).copy(
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
        val initial = name.trim().ifEmpty { stringResource(R.string.member_home_member_initial_fallback) }
            .first().uppercaseChar().toString()
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
    val roleText = when (role.uppercase()) {
        "ROLE_REPRESENTANTE" -> stringResource(R.string.member_home_role_representative)
        else -> stringResource(R.string.member_home_role_member)
    }

    val bg = if (highlighted) BrandPrimary.copy(alpha = 0.25f) else Color(0xFF3A3A3A)
    val fg = if (highlighted) TextPrimary else TextSecondary
    Surface(
        color = bg,
        contentColor = fg,
        shape = RoundedCornerShape(999.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = ButtonDefaults.outlinedButtonBorder(
            enabled = true
        ).copy(
            width = if (highlighted) 1.dp else 0.dp,
            brush = androidx.compose.ui.graphics.SolidColor(BorderColor)
        )
    ) {
        Text(
            roleText,
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
        Text(stringResource(R.string.common_error_oops), style = MaterialTheme.typography.titleLarge, color = Color(0xFFFF4D4F))
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
        ) {
            Text(stringResource(R.string.common_retry))
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
        Text(stringResource(R.string.common_empty_title), style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}


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
    if (roles.isEmpty()) return "ROLE_MIEMBRO"
    val r = roles.first().uppercase()
    return if (r.contains("REPRESENTANTE")) "ROLE_REPRESENTANTE" else "ROLE_MIEMBRO"
}

private fun resolveWelcomeName(s: MemberHomeUiState.Ready): String {
    fun <T> tryProp(name: String): T? = try {
        @Suppress("UNCHECKED_CAST")
        s::class.members.firstOrNull { it.name == name }?.call(s) as? T
    } catch (_: Throwable) { null }

    tryProp<String>("currentUserName")?.let { if (it.isNotBlank()) return it }

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

    val myId = tryProp<Long>("currentUserId") ?: tryProp<Int>("currentUserId")?.toLong()
    if (myId != null) {
        s.members.firstOrNull { it.id?.toLong() == myId }?.username?.let { if (it.isNotBlank()) return it }
    }

    tryProp<String>("sessionUsername")?.let { if (it.isNotBlank()) return it }
    tryProp<String>("username")?.let { if (it.isNotBlank()) return it }

    s.members.firstOrNull()?.username?.takeIf { it.isNotBlank() }?.let { return it }
    return "Usuario"
}