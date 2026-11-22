package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AppActionType
import com.example.data.AppFilterMode
import com.example.data.AppPermissionEntry
import com.example.data.InstalledAppInfo
import com.example.ui.TvPermUiState
import com.example.ui.components.DrawableAppIcon
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusPill
import com.example.ui.components.TvFocusCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldContainer
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.CoralAlertContainer
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.ElectricTealContainer
import com.example.ui.theme.EmeraldOk
import com.example.ui.theme.EmeraldOkContainer
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueContainer
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AppPermissionsScreen(
  uiState: TvPermUiState,
  onSearchQueryChange: (String) -> Unit,
  onFilterModeChange: (AppFilterMode) -> Unit,
  onSelectApp: (InstalledAppInfo?) -> Unit,
  onLaunchAppAction: (String, AppActionType) -> Unit,
  onRefreshApps: () -> Unit,
  onShowMessage: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val selectedApp = uiState.selectedApp

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val isDualPane = maxWidth >= 780.dp

    if (isDualPane) {
      Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        AppListPane(
          uiState = uiState,
          selectedPackage = selectedApp?.packageName,
          onSearchQueryChange = onSearchQueryChange,
          onFilterModeChange = onFilterModeChange,
          onSelectApp = onSelectApp,
          onRefreshApps = onRefreshApps,
          modifier = Modifier.weight(0.45f).fillMaxHeight(),
        )

        Surface(
          modifier = Modifier.weight(0.55f).fillMaxHeight(),
          shape = MaterialTheme.shapes.large,
          color = SlateSurface,
          border = BorderStroke(1.dp, SlateBorder),
        ) {
          if (selectedApp != null) {
            AppPermissionDetailPane(
              app = selectedApp,
              showBackButton = false,
              onBack = {},
              onLaunchAppAction = onLaunchAppAction,
              onShowMessage = onShowMessage,
            )
          } else {
            EmptyDetailPlaceholder(
              totalApps = uiState.allApps.size,
              modifier = Modifier.fillMaxSize(),
            )
          }
        }
      }
    } else {
      if (selectedApp != null) {
        BackHandler { onSelectApp(null) }
        AppPermissionDetailPane(
          app = selectedApp,
          showBackButton = true,
          onBack = { onSelectApp(null) },
          onLaunchAppAction = onLaunchAppAction,
          onShowMessage = onShowMessage,
          modifier = Modifier.fillMaxSize(),
        )
      } else {
        AppListPane(
          uiState = uiState,
          selectedPackage = null,
          onSearchQueryChange = onSearchQueryChange,
          onFilterModeChange = onFilterModeChange,
          onSelectApp = onSelectApp,
          onRefreshApps = onRefreshApps,
          modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        )
      }
    }
  }
}

@Composable
private fun AppListPane(
  uiState: TvPermUiState,
  selectedPackage: String?,
  onSearchQueryChange: (String) -> Unit,
  onFilterModeChange: (AppFilterMode) -> Unit,
  onSelectApp: (InstalledAppInfo) -> Unit,
  onRefreshApps: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      SectionHeader(
        title = "Installed App Permissions",
        subtitle = "${uiState.filteredApps.size} of ${uiState.allApps.size} apps • Tap to allow or inspect",
        icon = Icons.Filled.Apps,
        modifier = Modifier.weight(1f),
      )
      IconButton(
        onClick = onRefreshApps,
        modifier = Modifier.testTag("apps_refresh_button"),
      ) {
        Icon(
          imageVector = Icons.Filled.Refresh,
          contentDescription = "Reload installed apps",
          tint = ElectricTeal,
        )
      }
    }

    OutlinedTextField(
      value = uiState.searchQuery,
      onValueChange = onSearchQueryChange,
      placeholder = {
        Text(
          text = "Search app or package name…",
          style = MaterialTheme.typography.bodyMedium,
          color = TextMuted,
        )
      },
      leadingIcon = {
        Icon(
          imageVector = Icons.Filled.Search,
          contentDescription = null,
          tint = ElectricTeal,
        )
      },
      trailingIcon = {
        if (uiState.searchQuery.isNotEmpty()) {
          IconButton(
            onClick = { onSearchQueryChange("") },
            modifier = Modifier.testTag("apps_clear_search"),
          ) {
            Icon(
              imageVector = Icons.Filled.Clear,
              contentDescription = "Clear search",
              tint = TextSecondary,
            )
          }
        }
      },
      singleLine = true,
      shape = MaterialTheme.shapes.medium,
      colors =
        OutlinedTextFieldDefaults.colors(
          focusedBorderColor = ElectricTeal,
          unfocusedBorderColor = SlateBorder,
          focusedContainerColor = SlateSurface,
          unfocusedContainerColor = SlateSurface,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary,
        ),
      modifier = Modifier.fillMaxWidth().testTag("apps_search_input"),
    )

    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      AppFilterMode.entries.forEach { mode ->
        val selected = uiState.filterMode == mode
        FilterChip(
          selected = selected,
          onClick = { onFilterModeChange(mode) },
          label = { Text(mode.label, maxLines = 1) },
          colors =
            FilterChipDefaults.filterChipColors(
              selectedContainerColor = ElectricTealContainer,
              selectedLabelColor = ElectricTeal,
              containerColor = SlateSurface,
              labelColor = TextSecondary,
            ),
          modifier = Modifier.testTag("filter_chip_${mode.name.lowercase()}"),
        )
      }
    }

    when {
      uiState.isLoadingApps -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            CircularProgressIndicator(color = ElectricTeal)
            Text(
              text = "Scanning installed TV & user apps…",
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
            )
          }
        }
      }

      uiState.filteredApps.isEmpty() -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Apps,
              contentDescription = null,
              tint = TextMuted,
              modifier = Modifier.size(38.dp),
            )
            Text(
              text = "No matching apps found",
              style = MaterialTheme.typography.titleMedium,
              color = TextPrimary,
            )
            Text(
              text = "Try clearing the search box or switching to 'All Apps'.",
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
            )
          }
        }
      }

      else -> {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(bottom = 16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          items(uiState.filteredApps, key = { it.packageName }) { app ->
            AppRowCard(
              app = app,
              selected = app.packageName == selectedPackage,
              onClick = { onSelectApp(app) },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun AppRowCard(
  app: InstalledAppInfo,
  selected: Boolean,
  onClick: () -> Unit,
) {
  TvFocusCard(
    onClick = onClick,
    selected = selected,
    testTag = "app_card_${app.packageName}",
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      DrawableAppIcon(
        drawable = app.icon,
        contentDescription = "${app.appName} icon",
        modifier = Modifier.size(44.dp),
      )

      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = app.appName,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
          )
          if (app.isTvLeanbackApp) {
            StatusPill(
              text = "TV",
              containerColor = SkyBlueContainer,
              contentColor = SkyBlue,
              showDot = false,
            )
          }
        }
        Text(
          text = app.packageName,
          style = MaterialTheme.typography.labelSmall,
          color = TextMuted,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          StatusPill(
            text = "${app.grantedCount} Allowed",
            containerColor = EmeraldOkContainer,
            contentColor = EmeraldOk,
            showDot = false,
          )
          if (app.deniedCount > 0) {
            StatusPill(
              text = "${app.deniedCount} Not Granted",
              containerColor = AmberGoldContainer,
              contentColor = AmberGold,
              showDot = false,
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppPermissionDetailPane(
  app: InstalledAppInfo,
  showBackButton: Boolean,
  onBack: () -> Unit,
  onLaunchAppAction: (String, AppActionType) -> Unit,
  onShowMessage: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val clipboard = LocalClipboardManager.current

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(18.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        if (showBackButton) {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("app_detail_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to apps list",
              tint = ElectricTeal,
            )
          }
        }
        DrawableAppIcon(
          drawable = app.icon,
          contentDescription = "${app.appName} icon",
          modifier = Modifier.size(52.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = app.appName,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = "${app.packageName} • v${app.versionName}",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }

    item {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatusPill(
          text = "${app.grantedCount} Granted",
          containerColor = EmeraldOkContainer,
          contentColor = EmeraldOk,
        )
        StatusPill(
          text = "${app.deniedCount} Denied / Pending",
          containerColor =
            if (app.deniedCount > 0) AmberGoldContainer else SlateSurfaceVariant,
          contentColor = if (app.deniedCount > 0) AmberGold else TextSecondary,
        )
        if (app.isSystemApp) {
          StatusPill(
            text = "System App",
            containerColor = SlateSurfaceVariant,
            contentColor = TextSecondary,
            showDot = false,
          )
        }
      }
    }

    item {
      Surface(
        shape = MaterialTheme.shapes.medium,
        color = SlateSurfaceVariant.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, ElectricTeal.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.LockOpen,
              contentDescription = null,
              tint = ElectricTeal,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = "Enable / Manage Permissions Directly",
              style = MaterialTheme.typography.titleMedium,
              color = TextPrimary,
            )
          }
          Text(
            text =
              "Launch Android's permission screens directly for this app without needing Developer Options:",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Button(
              onClick = { onLaunchAppAction(app.packageName, AppActionType.MANAGE_PERMISSIONS) },
              colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal),
              modifier = Modifier.weight(1f).testTag("app_manage_perms_button"),
            ) {
              Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
              Spacer(Modifier.width(6.dp))
              Text("Allow Permissions", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(
              onClick = { onLaunchAppAction(app.packageName, AppActionType.APP_DETAILS) },
              modifier = Modifier.weight(1f).testTag("app_info_button"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
              Spacer(Modifier.width(6.dp))
              Text("App Info", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
          }

          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            SpecialChipButton("Overlay Access") {
              onLaunchAppAction(app.packageName, AppActionType.OVERLAY)
            }
            SpecialChipButton("Modify Settings") {
              onLaunchAppAction(app.packageName, AppActionType.WRITE_SETTINGS)
            }
            SpecialChipButton("Install Unknown") {
              onLaunchAppAction(app.packageName, AppActionType.UNKNOWN_SOURCES)
            }
            SpecialChipButton("Notifications") {
              onLaunchAppAction(app.packageName, AppActionType.NOTIFICATIONS)
            }
            SpecialChipButton("All Files") {
              onLaunchAppAction(app.packageName, AppActionType.ALL_FILES)
            }
          }
        }
      }
    }

    item {
      Text(
        text = "Declared Manifest Permissions (${app.permissions.size})",
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
      )
    }

    if (app.permissions.isEmpty()) {
      item {
        Surface(
          shape = MaterialTheme.shapes.medium,
          color = SlateSurfaceVariant.copy(alpha = 0.4f),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = "This app does not request any permissions in its manifest.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(16.dp),
          )
        }
      }
    } else {
      items(app.permissions, key = { it.permissionName }) { perm ->
        PermissionEntryCard(
          entry = perm,
          onOpenSystemPerms = {
            onLaunchAppAction(app.packageName, AppActionType.MANAGE_PERMISSIONS)
          },
          onCopyAdbGrant = {
            val cmd = "adb shell pm grant ${app.packageName} ${perm.permissionName}"
            clipboard.setText(AnnotatedString(cmd))
            onShowMessage("Copied: $cmd")
          },
        )
      }
    }

    item { Spacer(Modifier.height(12.dp)) }
  }
}

@Composable
private fun SpecialChipButton(label: String, onClick: () -> Unit) {
  OutlinedButton(
    onClick = onClick,
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
  ) {
    Text(text = label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
  }
}

@Composable
private fun PermissionEntryCard(
  entry: AppPermissionEntry,
  onOpenSystemPerms: () -> Unit,
  onCopyAdbGrant: () -> Unit,
) {
  TvFocusCard(
    onClick = onOpenSystemPerms,
    containerColor = SlateSurfaceVariant.copy(alpha = 0.45f),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
        imageVector = Icons.Filled.CheckCircle,
        contentDescription = null,
        tint = if (entry.isGranted) EmeraldOk else CoralAlert,
        modifier = Modifier.size(18.dp),
      )
      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = entry.shortName,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
          )
          StatusPill(
            text = entry.groupLabel,
            containerColor = SkyBlueContainer,
            contentColor = SkyBlue,
            showDot = false,
          )
        }
        Text(
          text = entry.permissionName,
          style = MaterialTheme.typography.labelSmall,
          color = TextMuted,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      StatusPill(
        text = if (entry.isGranted) "Allowed" else "Denied",
        containerColor = if (entry.isGranted) EmeraldOkContainer else CoralAlertContainer,
        contentColor = if (entry.isGranted) EmeraldOk else CoralAlert,
        showDot = false,
      )
      if (!entry.isGranted) {
        IconButton(
          onClick = onCopyAdbGrant,
          modifier = Modifier.size(36.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.ContentCopy,
            contentDescription = "Copy ADB grant command",
            tint = ElectricTeal,
            modifier = Modifier.size(16.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun EmptyDetailPlaceholder(
  totalApps: Int,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.padding(24.dp), contentAlignment = Alignment.Center) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
        imageVector = Icons.Filled.Shield,
        contentDescription = null,
        tint = ElectricTeal,
        modifier = Modifier.size(44.dp),
      )
      Text(
        text = "Select an App to Manage Permissions",
        style = MaterialTheme.typography.titleLarge,
        color = TextPrimary,
      )
      Text(
        text =
          "Pick any of the $totalApps apps on the left to inspect every permission and jump straight to its Allow/Permissions screen.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary,
      )
    }
  }
}
