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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spliteasy.spliteasy.R
import com.spliteasy.spliteasy.ui.member.contribs.MembContribsScreen
import com.spliteasy.spliteasy.ui.member.settings.MembSettingsScreen
import com.spliteasy.spliteasy.ui.member.status.MembStatusScreen

sealed class MemberDest(
    val route: String,
    val label: @Composable () -> String,
    val icon: ImageVector
) {
    data object Home          : MemberDest("member/home",          { stringResource(R.string.member_nav_tab_home) },     Icons.Rounded.Home)
    data object Contributions : MemberDest("member/contributions", { stringResource(R.string.member_nav_tab_contribs) }, Icons.Rounded.Wallet)
    data object Status        : MemberDest("member/status",        { stringResource(R.string.member_nav_tab_status) },    Icons.Rounded.Assessment)
    data object Settings      : MemberDest("member/settings",      { stringResource(R.string.member_nav_tab_settings) },  Icons.Rounded.Settings)
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

    val fallbackUsername = stringResource(R.string.common_user_fallback)
    val fallbackInitial = stringResource(R.string.member_home_member_initial_fallback)

    val currentUserName = remember(homeState, fallbackUsername) {
        (homeState as? MemberHomeUiState.Ready)?.currentUserName ?: fallbackUsername
    }
    val initial = currentUserName.trim().ifBlank { fallbackInitial }.first().uppercaseChar().toString()

    val currentUserId: Long? = (homeState as? MemberHomeUiState.Ready)?.currentUserId

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            MemberTopBar(
                title = stringResource(R.string.member_nav_app_name),
                subtitle = stringResource(R.string.member_nav_title),
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
                .background(MaterialTheme.colorScheme.background)
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
                            Text(stringResource(R.string.member_nav_loading_user), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            .background(MaterialTheme.colorScheme.background)
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
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = "$title — $username",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }

            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = stringResource(R.string.member_nav_cd_logout),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
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
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
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
                        imageVector = dest.icon,
                        contentDescription = labelText
                    )
                },
                label = {
                    Text(
                        text = labelText,
                        maxLines = 1
                    )
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