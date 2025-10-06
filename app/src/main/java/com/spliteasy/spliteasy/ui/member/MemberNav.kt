package com.spliteasy.spliteasy.ui.member

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.*
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
import com.spliteasy.spliteasy.ui.member.contribs.MembContribsViewModel // ✅ Import correcto
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset

/* ---------------------- Paleta SpliteEasy (oscura) ---------------------- */
private val BrandPrimary   = Color(0xFF1565C0)
private val BrandSecondary = Color(0xFFFF6F00)
private val SuccessColor   = Color(0xFF2E7D32)
private val InfoColor      = Color(0xFF4F46E5)

private val BgMain   = Color(0xFF1A1A1A) // --background-main
private val BgCard   = Color(0xFF2D2D2D) // --background-card
private val Border   = Color(0xFF404040) // --border-color
private val TextPri  = Color(0xFFF8F9FA) // --text-primary
private val TextSec  = Color(0xFFADB5BD) // --text-secondary

/* --------------------------- Destinos de Miembro --------------------------- */
sealed class MemberDest(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Home           : MemberDest("member/home",           "Inicio",         Icons.Rounded.Home)
    data object Contributions  : MemberDest("member/contributions",  "Contribuciones", Icons.Rounded.Wallet)
    data object Status         : MemberDest("member/status",         "Estado",         Icons.Rounded.Assessment)
    data object Settings       : MemberDest("member/settings",       "Ajustes",        Icons.Rounded.Settings)
}
private val memberTabs = listOf(MemberDest.Home, MemberDest.Contributions, MemberDest.Status, MemberDest.Settings)

/* ------------------------- Root de navegación miembro ------------------------- */
@Composable
fun MemberNavRoot(modifier: Modifier = Modifier, onLogout: () -> Unit = {}) {
    val nav = rememberNavController()

    val homeVm: MemberHomeViewModel = hiltViewModel()
    val homeState by homeVm.uiState.collectAsState()

    LaunchedEffect(Unit) { homeVm.load() }

    val currentUserName: String = remember(homeState) {
        (homeState as? MemberHomeUiState.Ready)?.currentUserName ?: "Usuario"
    }
    val initial = currentUserName.trim().ifBlank { "U" }.first().uppercaseChar().toString()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgMain,
        // 👇 Esto hace que Scaffold respete safe areas (status bar / notch / gesture nav)
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
                    MemberHomeScreen(onAddExpense = {}, onOpenExpense = {})
                }
                composable(MemberDest.Contributions.route) { MembContribsScreen() }
                composable(MemberDest.Status.route)        { MembStatusScreen() }
                composable(MemberDest.Settings.route)      { MembSettingsScreen() }
            }
        }
    }
}


/* --------------------------------- TopBar --------------------------------- */
@Composable
private fun MemberTopBar(
    title: String,
    subtitle: String,
    initial: String,
    username: String,
    onLogout: () -> Unit
) {
    // Contenedor de la app bar respetando la altura de la status bar
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // 👇 Respeta el status bar / notch SOLO en el top bar
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
            // Avatar (iniciales)
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

            // Botón cerrar sesión
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.Rounded.Logout,
                    contentDescription = "Cerrar sesión",
                    tint = TextPri
                )
            }
        }

        // Borde inferior (Divider) para separar y evitar ver borde en todos los lados
        Divider(color = Border, thickness = 1.dp)
    }
}

/* ------------------------------- BottomBar ------------------------------- */
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
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
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

/* --------------------- Stubs (reemplaza con tus pantallas) --------------------- */
@Composable
private fun MembContribsScreen(vm: MembContribsViewModel = hiltViewModel()) {
    Box(Modifier.fillMaxSize().background(BgMain), contentAlignment = Alignment.Center) {
        Text("Contribuciones", color = TextPri)
    }
}

@Composable
private fun MembStatusScreen() {
    Box(Modifier.fillMaxSize().background(BgMain), contentAlignment = Alignment.Center) {
        Text("Estado", color = TextPri)
    }
}

@Composable
private fun MembSettingsScreen() {
    Box(Modifier.fillMaxSize().background(BgMain), contentAlignment = Alignment.Center) {
        Text("Ajustes", color = TextPri)
    }
}
