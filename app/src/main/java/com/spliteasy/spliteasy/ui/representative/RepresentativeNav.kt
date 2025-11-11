package com.spliteasy.spliteasy.ui.representative

import android.app.Application
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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.spliteasy.spliteasy.ui.representative.home.RepCreateHouseholdScreen
import com.spliteasy.spliteasy.ui.representative.members.RepMembersScreen
import com.spliteasy.spliteasy.ui.representative.bills.RepBillsScreen
import com.spliteasy.spliteasy.ui.representative.contributions.RepContributionsScreen
import com.spliteasy.spliteasy.ui.member.settings.MembSettingsScreen
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.spliteasy.spliteasy.R

sealed class RepDest(
    val route: String,
    val label: @Composable () -> String,
    val icon: ImageVector
) {
    data object Home          : RepDest("rep/home",          { stringResource(R.string.rep_nav_tab_home) },     Icons.Rounded.Home)
    data object Members       : RepDest("rep/members",       { stringResource(R.string.rep_nav_tab_members) },  Icons.Rounded.Group)
    data object Bills         : RepDest("rep/bills",         { stringResource(R.string.rep_nav_tab_bills) },    Icons.Rounded.ReceiptLong)
    data object Contributions : RepDest("rep/contributions", { stringResource(R.string.rep_nav_tab_contribs) }, Icons.Rounded.Wallet)
    data object Settings      : RepDest("rep/settings",      { stringResource(R.string.rep_nav_tab_settings) }, Icons.Rounded.Settings)

    data object CreateHousehold : RepDest("rep/create-household", { stringResource(R.string.rep_nav_tab_create_household) }, Icons.Rounded.Home)
}
private val repTabs = listOf(
    RepDest.Home, RepDest.Members, RepDest.Bills, RepDest.Contributions, RepDest.Settings
)

sealed interface RepHeaderUiState {
    data object Loading : RepHeaderUiState
    data class Ready(val userId: Long, val username: String) : RepHeaderUiState
    data class Error(val message: String) : RepHeaderUiState
}

@HiltViewModel
class RepHeaderViewModel @Inject constructor(
    app: Application,
    private val accountRepo: AccountRepository
) : AndroidViewModel(app) {

    private val _ui = MutableStateFlow<RepHeaderUiState>(RepHeaderUiState.Loading)
    val ui = _ui.asStateFlow()

    private val app: Application = getApplication()

    fun load() {
        viewModelScope.launch {
            accountRepo.me()
                .onSuccess { profile ->
                    val id: Long = (profile.id ?: 0L)
                    val name: String = (profile.username ?: "").ifBlank { app.getString(R.string.rep_nav_vm_default_name) }
                    _ui.value = RepHeaderUiState.Ready(userId = id, username = name)
                }
                .onFailure { t ->
                    _ui.value = RepHeaderUiState.Error(t.message ?: app.getString(R.string.rep_nav_vm_error_load_user))
                }
        }
    }
}

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

@Composable
fun RepresentativeNavRoot(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
) {
    val nav = rememberNavController()
    val headerVm: RepHeaderViewModel = hiltViewModel()
    val headerState by headerVm.ui.collectAsState()
    LaunchedEffect(Unit) { headerVm.load() }
    val logoutVm: RepLogoutViewModel = hiltViewModel()

    val fallbackName = stringResource(R.string.rep_nav_vm_default_name)
    val fallbackInitial = stringResource(R.string.rep_nav_initial_fallback)

    val currentUserName = remember(headerState, fallbackName) {
        (headerState as? RepHeaderUiState.Ready)?.username ?: fallbackName
    }
    val initial = currentUserName.trim().ifBlank { fallbackInitial }.first().uppercaseChar().toString()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            RepTopBar(
                title = stringResource(R.string.rep_nav_app_name),
                subtitle = stringResource(R.string.rep_nav_title),
                initial = initial,
                username = currentUserName,
                onLogout = { logoutVm.logout(onAfter = onLogout) }
            )
        },
        bottomBar = { RepBottomBar(tabs = repTabs, nav = nav) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
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
                composable(RepDest.Members.route) { RepMembersScreen() }
                composable(RepDest.Bills.route)         { RepBillsScreen() }
                composable(RepDest.Contributions.route) { RepContributionsScreen() }
                composable(RepDest.Settings.route)      { MembSettingsScreen() }
            }
        }
    }
}

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
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = .2f),
                shape = CircleShape
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        initial,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "$title — $username",
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onLogout) {
                Icon(
                    Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = stringResource(R.string.rep_nav_cd_logout),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
    }
}

@Composable
private fun RepBottomBar(tabs: List<RepDest>, nav: NavHostController) {
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
        tabs.forEach { dest ->
            val selected = currentRoute == dest.route
            val labelText = dest.label()

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
                        contentDescription = labelText,
                    )
                },
                label = {
                    Text(labelText, maxLines = 1)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun StubCenter(text: String) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = MaterialTheme.colorScheme.onBackground)
    }
}