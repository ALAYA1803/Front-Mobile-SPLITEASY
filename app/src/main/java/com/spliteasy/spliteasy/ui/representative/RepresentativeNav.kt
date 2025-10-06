package com.spliteasy.spliteasy.ui.representative

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spliteasy.spliteasy.ui.representative.home.RepHomeScreen
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import com.spliteasy.spliteasy.data.local.TokenDataStore
import com.spliteasy.spliteasy.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.spliteasy.spliteasy.ui.representative.home.RepCreateHouseholdScreen
import com.spliteasy.spliteasy.ui.representative.home.create.RepCreateHouseholdViewModel

/* Paleta */
private val BrandPrimary   = Color(0xFF1565C0)
private val BgMain         = Color(0xFF1A1A1A)
private val BgCard         = Color(0xFF2D2D2D)
private val Border         = Color(0xFF404040)
private val TextPri        = Color(0xFFF8F9FA)
private val TextSec        = Color(0xFFADB5BD)

/* ----- Destinos ----- */
sealed class RepDest(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    data object Home          : RepDest("rep/home",          "Inicio",         Icons.Rounded.Home)
    data object Members       : RepDest("rep/members",       "Miembros",       Icons.Rounded.Group)
    data object Bills         : RepDest("rep/bills",         "Facturas",       Icons.Rounded.ReceiptLong)
    data object Contributions : RepDest("rep/contributions", "Contribuciones", Icons.Rounded.Wallet)
    data object Settings      : RepDest("rep/settings",      "Ajustes",        Icons.Rounded.Settings)

    data object CreateHousehold : RepDest("rep/create-household", "Crear hogar", Icons.Rounded.Home)
}
private val repTabs = listOf(
    RepDest.Home, RepDest.Members, RepDest.Bills, RepDest.Contributions, RepDest.Settings
)

/* ===== ViewModel cabecera (username / userId) ===== */
sealed interface RepHeaderUiState {
    data object Loading : RepHeaderUiState
    data class Ready(val userId: Long, val username: String) : RepHeaderUiState
    data class Error(val message: String) : RepHeaderUiState
}

@HiltViewModel
class RepHeaderViewModel @Inject constructor(
    private val accountRepo: AccountRepository
) : ViewModel() {

    private val _ui = MutableStateFlow<RepHeaderUiState>(RepHeaderUiState.Loading)
    val ui = _ui.asStateFlow()

    fun load() {
        viewModelScope.launch {
            accountRepo.me()
                .onSuccess { profile ->
                    val id: Long = (profile.id ?: 0L)
                    val name: String = (profile.username ?: "").ifBlank { "Representante" }
                    _ui.value = RepHeaderUiState.Ready(userId = id, username = name)
                }
                .onFailure { t ->
                    _ui.value = RepHeaderUiState.Error(t.message ?: "No se pudo cargar el usuario")
                }
        }
    }
}

/* ===== ViewModel logout (limpia DataStore) ===== */
@HiltViewModel
class RepLogoutViewModel @Inject constructor(
    private val tokenStore: TokenDataStore
) : ViewModel() {
    fun logout(onAfter: () -> Unit) {
        viewModelScope.launch {
            runCatching { tokenStore.clear() }
            onAfter()
        }
    }
}

/* ===== Root ===== */
@Composable
fun RepresentativeNavRoot(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},              // <- AppNav debe inyectar navegación a LOGIN
) {
    val nav = rememberNavController()

    // VM cabecera (username)
    val headerVm: RepHeaderViewModel = hiltViewModel()
    val headerState by headerVm.ui.collectAsState()
    LaunchedEffect(Unit) { headerVm.load() }

    // VM logout (limpieza de DataStore)
    val logoutVm: RepLogoutViewModel = hiltViewModel()

    val currentUserName = remember(headerState) {
        (headerState as? RepHeaderUiState.Ready)?.username ?: "Representante"
    }
    val initial = currentUserName.trim().ifBlank { "R" }.first().uppercaseChar().toString()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgMain,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            RepTopBar(
                title = "SplitEasy",
                subtitle = "Panel del Representante",
                initial = initial,
                username = currentUserName,
                onLogout = { logoutVm.logout(onAfter = onLogout) }  // ✅ limpia token y luego navega
            )
        },
        bottomBar = { RepBottomBar(tabs = repTabs, nav = nav) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BgMain)
        ) {
            NavHost(
                navController = nav,
                startDestination = RepDest.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(RepDest.Home.route) {
                    RepHomeScreen(onCreateHousehold = { nav.navigate(RepDest.CreateHousehold.route) })
                }
                composable(RepDest.CreateHousehold.route) {
                    RepCreateHouseholdScreen(
                        onCancel = { nav.popBackStack() },
                        onCreated = {
                            nav.navigate(RepDest.Home.route) {
                                popUpTo(RepDest.Home.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(RepDest.Members.route)       { StubCenter("Miembros") }
                composable(RepDest.Bills.route)         { StubCenter("Facturas") }
                composable(RepDest.Contributions.route) { StubCenter("Contribuciones") }
                composable(RepDest.Settings.route)      { StubCenter("Ajustes") }
            }
        }
    }
}

/* ===== TopBar ===== */
@Composable
private fun RepTopBar(
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
            Surface(
                color = BrandPrimary.copy(alpha = .2f),
                shape = CircleShape
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        initial,
                        color = BrandPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "$title — $username",
                    color = TextPri,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(subtitle, color = TextSec, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onLogout) {
                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "Cerrar sesión", tint = TextPri)
            }
        }
        Divider(color = Border, thickness = 1.dp)
    }
}

@Composable
private fun RepBottomBar(tabs: List<RepDest>, nav: NavHostController) {
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar(containerColor = BgCard, tonalElevation = 0.dp) {
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
                        dest.icon,
                        contentDescription = dest.label,
                        tint = if (selected) BrandPrimary else TextSec
                    )
                },
                label = {
                    Text(dest.label, color = if (selected) TextPri else TextSec, maxLines = 1)
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

@Composable
private fun StubCenter(text: String) {
    Box(Modifier.fillMaxSize().background(BgMain), contentAlignment = Alignment.Center) {
        Text(text, color = TextPri)
    }
}
