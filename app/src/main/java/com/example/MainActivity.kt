package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.TvPermViewModel
import com.example.ui.components.FallbackDiagnosticsDialog
import com.example.ui.screens.AppPermissionsScreen
import com.example.ui.screens.QuickHubScreen
import com.example.ui.screens.SpecialAccessScreen
import com.example.ui.screens.TvAdbGuideScreen
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.ElectricTealContainer
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TvPermGuardTheme

const val ROUTE_HUB = "hub"
const val ROUTE_APPS = "apps"
const val ROUTE_SPECIAL = "special"
const val ROUTE_GUIDE = "guide"

private data class NavDestinationItem(
  val route: String,
  val labelRes: Int,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String,
)

private val TopNavItems =
  listOf(
    NavDestinationItem(
      route = ROUTE_HUB,
      labelRes = R.string.nav_hub,
      selectedIcon = Icons.Filled.Dashboard,
      unselectedIcon = Icons.Outlined.Dashboard,
      testTag = "nav_hub",
    ),
    NavDestinationItem(
      route = ROUTE_APPS,
      labelRes = R.string.nav_apps,
      selectedIcon = Icons.Filled.Apps,
      unselectedIcon = Icons.Outlined.Apps,
      testTag = "nav_apps",
    ),
    NavDestinationItem(
      route = ROUTE_SPECIAL,
      labelRes = R.string.nav_special,
      selectedIcon = Icons.Filled.AdminPanelSettings,
      unselectedIcon = Icons.Outlined.AdminPanelSettings,
      testTag = "nav_special",
    ),
    NavDestinationItem(
      route = ROUTE_GUIDE,
      labelRes = R.string.nav_guide,
      selectedIcon = Icons.Filled.Terminal,
      unselectedIcon = Icons.Outlined.Terminal,
      testTag = "nav_guide",
    ),
  )

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      TvPermGuardTheme {
        TvPermGuardApp()
      }
    }
  }
}

@Composable
fun TvPermGuardApp(viewModel: TvPermViewModel = viewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val navController = rememberNavController()
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = backStackEntry?.destination?.route ?: ROUTE_HUB
  val snackbarHostState = remember { SnackbarHostState() }
  val lifecycleOwner = LocalLifecycleOwner.current

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        viewModel.refreshAll()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  LaunchedEffect(uiState.snackbarMessage) {
    val msg = uiState.snackbarMessage
    if (!msg.isNullOrBlank()) {
      snackbarHostState.showSnackbar(msg)
      viewModel.clearSnackbar()
    }
  }

  val navigateToRoute: (String) -> Unit = { route ->
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  uiState.unavailableDialogTarget?.let { unavailable ->
    FallbackDiagnosticsDialog(
      unavailable = unavailable,
      localIp = uiState.deviceStatus.localIpAddress,
      onOpenAboutTv = { viewModel.launchTargetById("about_tv_build") },
      onOpenFullSettings = { viewModel.launchTargetById("full_system_settings") },
      onCopiedMessage = { msg -> viewModel.showMessage(msg) },
      onDismiss = { viewModel.dismissUnavailableDialog() },
    )
  }

  BoxWithConstraints(
    modifier = Modifier.fillMaxSize().background(DeepNavyBg),
  ) {
    val useNavigationRail = maxWidth >= 700.dp

    Scaffold(
      containerColor = DeepNavyBg,
      contentWindowInsets = WindowInsets.safeDrawing,
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      bottomBar = {
        if (!useNavigationRail) {
          NavigationBar(
            containerColor = SlateSurface,
            tonalElevation = 4.dp,
          ) {
            TopNavItems.forEach { item ->
              val selected = currentRoute == item.route
              val label = stringResource(item.labelRes)
              NavigationBarItem(
                selected = selected,
                onClick = { navigateToRoute(item.route) },
                icon = {
                  Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = label,
                  )
                },
                label = { Text(text = label, maxLines = 1) },
                colors =
                  NavigationBarItemDefaults.colors(
                    selectedIconColor = ElectricTeal,
                    selectedTextColor = ElectricTeal,
                    indicatorColor = ElectricTealContainer,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                  ),
                modifier = Modifier.testTag(item.testTag),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
      ) {
        if (useNavigationRail) {
          NavigationRail(
            containerColor = SlateSurface,
            modifier = Modifier.fillMaxHeight(),
            header = {
              Box(
                modifier = Modifier.padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Filled.Tv,
                  contentDescription = stringResource(R.string.app_name),
                  tint = ElectricTeal,
                  modifier = Modifier.size(28.dp),
                )
              }
            },
          ) {
            Spacer(Modifier.weight(0.2f))
            TopNavItems.forEach { item ->
              val selected = currentRoute == item.route
              val label = stringResource(item.labelRes)
              NavigationRailItem(
                selected = selected,
                onClick = { navigateToRoute(item.route) },
                icon = {
                  Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = label,
                  )
                },
                label = { Text(text = label, maxLines = 1) },
                colors =
                  NavigationRailItemDefaults.colors(
                    selectedIconColor = ElectricTeal,
                    selectedTextColor = ElectricTeal,
                    indicatorColor = ElectricTealContainer,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                  ),
                modifier = Modifier.testTag(item.testTag),
              )
            }
            Spacer(Modifier.weight(1f))
          }
        }

        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
          NavHost(
            navController = navController,
            startDestination = ROUTE_HUB,
            modifier = Modifier.fillMaxSize(),
          ) {
            composable(ROUTE_HUB) {
              QuickHubScreen(
                uiState = uiState,
                onLaunchTarget = viewModel::launchTarget,
                onLaunchTargetById = viewModel::launchTargetById,
                onNavigateToApps = { navigateToRoute(ROUTE_APPS) },
                onRefresh = viewModel::refreshAll,
              )
            }
            composable(ROUTE_APPS) {
              AppPermissionsScreen(
                uiState = uiState,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onFilterModeChange = viewModel::updateFilterMode,
                onSelectApp = viewModel::selectApp,
                onLaunchAppAction = viewModel::launchAppAction,
                onRefreshApps = viewModel::loadApps,
                onShowMessage = viewModel::showMessage,
              )
            }
            composable(ROUTE_SPECIAL) {
              SpecialAccessScreen(
                uiState = uiState,
                onLaunchTarget = viewModel::launchTarget,
                onRefresh = viewModel::refreshAll,
              )
            }
            composable(ROUTE_GUIDE) {
              TvAdbGuideScreen(
                uiState = uiState,
                onLaunchTargetById = viewModel::launchTargetById,
                onShowMessage = viewModel::showMessage,
              )
            }
          }
        }
      }
    }
  }
}
