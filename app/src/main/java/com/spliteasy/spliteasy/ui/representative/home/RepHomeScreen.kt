package com.spliteasy.spliteasy.ui.representative.home

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.spliteasy.spliteasy.R
import com.spliteasy.spliteasy.ui.theme.InfoColor
import com.spliteasy.spliteasy.ui.theme.SuccessColor


@Composable
fun RepHomeScreen(
    vm: RepHomeViewModel = hiltViewModel(),
    onCreateHousehold: () -> Unit = {}
) {
    val ui by vm.ui.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when {
            ui.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            ui.error != null -> ErrorBox(
                message = ui.error ?: stringResource(R.string.member_home_error_generic),
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
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = ButtonDefaults.outlinedButtonBorder(
                enabled = true
            ).copy(
                width = 1.dp,
                brush = SolidColor(MaterialTheme.colorScheme.outline)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    stringResource(R.string.rep_home_onboarding_welcome),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    stringResource(R.string.rep_home_onboarding_subtitle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = onCreate,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text(stringResource(R.string.rep_home_onboarding_button)) }
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
                householdDesc = ui.household?.description.orEmpty(),
                currency = ui.currency
            )
        }

        item {
            Section(title = stringResource(R.string.rep_home_section_summary)) {
                Column(
                    Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            title = stringResource(R.string.rep_home_stat_members),
                            value = ui.membersCount,
                            icon = Icons.Rounded.Groups,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = stringResource(R.string.rep_home_stat_bills),
                            value = ui.billsCount,
                            icon = Icons.Rounded.ReceiptLong,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            title = stringResource(R.string.rep_home_stat_contributions),
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
    householdDesc: String,
    currency: String
) {
    val gradient = Brush.verticalGradient(
        listOf(MaterialTheme.colorScheme.primary.copy(alpha = .22f), Color.Transparent)
    )
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(top = 18.dp, bottom = 8.dp)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                stringResource(R.string.rep_home_dashboard_title),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (householdName.isBlank()) stringResource(R.string.common_fallback_dash) else householdName,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (householdDesc.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(householdDesc, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    enabled = true,
                    label = { Text(stringResource(R.string.rep_home_dashboard_currency, currency)) },
                    leadingIcon = { /* … */ },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                )
                AssistChip(
                    onClick = {},
                    label = { Text(stringResource(R.string.rep_home_dashboard_status_active)) },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = SuccessColor
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                )
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
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
        ActionChip(
            stringResource(R.string.rep_home_action_members),
            Icons.Rounded.Groups,
            MaterialTheme.colorScheme.primary
        ) { onMembers?.invoke() }
        ActionChip(
            stringResource(R.string.rep_home_action_bills),
            Icons.Rounded.ReceiptLong,
            MaterialTheme.colorScheme.secondary
        ) { onBills?.invoke() }
        ActionChip(
            stringResource(R.string.rep_home_action_contributions),
            Icons.Rounded.Wallet,
            InfoColor
        ) { onContrib?.invoke() }
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun ActionChip(
    text: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    FilterChip(
        selected = false,
        onClick = onClick,
        enabled = true,
        label = { Text(text) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = tint) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
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
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        content()
    }
}

@Composable
private fun StatCard(
    title: String,
    value: Int,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val animated by animateIntAsState(targetValue = value, label = "stat")

    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    animated.toString(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
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
        Text(
            stringResource(R.string.common_error_oops),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) { Text(stringResource(R.string.common_retry)) }
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