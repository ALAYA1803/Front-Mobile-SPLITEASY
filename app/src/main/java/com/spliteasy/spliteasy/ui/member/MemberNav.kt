package com.spliteasy.spliteasy.ui.member

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spliteasy.spliteasy.ui.member.contribs.MembContribsScreen
import com.spliteasy.spliteasy.ui.member.settings.MembSettingsScreen
import com.spliteasy.spliteasy.ui.member.status.MembStatusScreen

private val BrandPrimary = Color(0xFF1565C0)
private val BgMain       = Color(0xFF1A1A1A)
private val BgCard       = Color(0xFF2D2D2D)
private val Border       = Color(0xFF404040)
private val TextPri      = Color(0xFFF8F9FA)
private val TextSec      = Color(0xFFADB5BD)

sealed class MemberDest(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    data object Home          : MemberDest("member/home",          "Inicio",         Icons.Rounded.Home)
    data object Contributions : MemberDest("member/contributions", "Contribuciones", Icons.Rounded.Wallet)
    data object Status        : MemberDest("member/status",        "Estado",         Icons.Rounded.Assessment)
    data object Settings      : MemberDest("member/settings",      "Ajustes",        Icons.Rounded.Settings)
}
private val memberTabs = listOf(
    MemberDest.Home, MemberDest.Contributions, MemberDest.Status, MemberDest.Settings
)

@Composable
fun MemberNavRoot(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {}
) {
    val nav = rememberNavController()

    val homeVm: MemberHomeViewModel = hiltViewModel()
    val homeState by homeVm.uiState.collectAsState()
    LaunchedEffect(Unit) { homeVm.load() }

    val currentUserName = remember(homeState) {
        (homeState as? MemberHomeUiState.Ready)?.currentUserName ?: "Usuario"
    }
    val initial = currentUserName.trim().ifBlank { "U" }.first().uppercaseChar().toString()

    val currentUserId: Long? = (homeState as? MemberHomeUiState.Ready)?.currentUserId

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgMain,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            MemberTopBar(
                title = "SpliteEasy",
                subtitle = "Panel de Miembro",
                initial = initial,
                username = currentUserName,
                onLogout = onLogout
            )
        },
        bottomBar = { MemberBottomBar(tabs = memberTabs, nav = nav) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BgMain)
        ) {
            NavHost(
                navController = nav,
                startDestination = MemberDest.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(MemberDest.Home.route) {
                    MemberHomeScreen(
                        onAddExpense = { /* TODO */ },
                        onOpenExpense = { /* TODO */ }
                    )
                }

                composable(MemberDest.Contributions.route) {
                    if (currentUserId != null && currentUserId > 0) {
                        MembContribsScreen(currentUserId = currentUserId)
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Cargando usuario…", color = TextSec)
                        }
                    }
                }

                composable(MemberDest.Status.route) {
                    MembStatusScreen()
                }

                composable(MemberDest.Settings.route) {
                    MembSettingsScreen()
                }
            }
        }
    }
}

@Composable
private fun MemberTopBar(
    title: String,
    subtitle: String,
    initial: String,
    username: String,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .background(BgMain)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BrandPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = BrandPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = "$title — $username",
                    color = TextPri,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = subtitle, color = TextSec, style = MaterialTheme.typography.bodySmall)
            }

            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = "Cerrar sesión",
                    tint = TextPri
                )
            }
        }

        HorizontalDivider(color = Border, thickness = 1.dp)
    }
}

@Composable
private fun MemberBottomBar(
    tabs: List<MemberDest>,
    nav: androidx.navigation.NavHostController
) {
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar(
        containerColor = BgCard,
        contentColor = TextPri,
        tonalElevation = 0.dp
    ) {
        tabs.forEach { dest ->
            val selected = currentRoute == dest.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    nav.navigate(dest.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(nav.graph.findStartDestination().id) {
                            saveState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = dest.icon,
                        contentDescription = dest.label,
                        tint = if (selected) BrandPrimary else TextSec
                    )
                },
                label = {
                    Text(
                        text = dest.label,
                        color = if (selected) TextPri else TextSec,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandPrimary,
                    selectedTextColor = TextPri,
                    indicatorColor = BrandPrimary.copy(alpha = 0.12f),
                    unselectedIconColor = TextSec,
                    unselectedTextColor = TextSec
                )
            )
        }
    }
}
