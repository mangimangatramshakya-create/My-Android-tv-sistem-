package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.HiddenSettingTarget
import com.example.data.TargetCategory
import com.example.ui.TvPermUiState
import com.example.ui.components.AvailabilityBadge
import com.example.ui.components.HeroConsoleBanner
import com.example.ui.components.SectionHeader
import com.example.ui.components.TvFocusCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldContainer
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.ElectricTealContainer
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueContainer
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun QuickHubScreen(
  uiState: TvPermUiState,
  onLaunchTarget: (HiddenSettingTarget) -> Unit,
  onLaunchTargetById: (String) -> Unit,
  onNavigateToApps: () -> Unit,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val priorityIds =
    listOf("accessibility", "developer_options", "usb_wireless_debugging", "all_permissions_hub")
  val priorityTargets =
    priorityIds.mapNotNull { id -> uiState.hiddenTargets.firstOrNull { it.id == id } }
  val secondaryTargets =
    uiState.hiddenTargets.filterNot { priorityIds.contains(it.id) }

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val isWide = maxWidth >= 700.dp

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        HeroConsoleBanner(deviceStatus = uiState.deviceStatus)
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          SectionHeader(
            title = "Essential TV Unblockers",
            subtitle = "Direct intents for settings commonly hidden by TV makers",
            icon = Icons.Filled.Security,
            modifier = Modifier.weight(1f),
          )
          IconButton(
            onClick = onRefresh,
            modifier = Modifier.testTag("refresh_hub_button"),
          ) {
            Icon(
              imageVector = Icons.Filled.Refresh,
              contentDescription = "Refresh TV status",
              tint = ElectricTeal,
            )
          }
        }
      }

      if (isWide) {
        val rows = priorityTargets.chunked(2)
        items(rows) { rowItems ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            rowItems.forEach { target ->
              PriorityTargetCard(
                target = target,
                onClick = { onLaunchTarget(target) },
                modifier = Modifier.weight(1f),
              )
            }
            if (rowItems.size == 1) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
      } else {
        items(priorityTargets, key = { it.id }) { target ->
          PriorityTargetCard(
            target = target,
            onClick = { onLaunchTarget(target) },
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }

      item {
        Surface(
          shape = MaterialTheme.shapes.medium,
          color = SlateSurfaceVariant.copy(alpha = 0.7f),
          border = BorderStroke(1.dp, ElectricTeal.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Apps,
              contentDescription = null,
              tint = ElectricTeal,
              modifier = Modifier.size(28.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Manual Permission Control (No Dev Options Needed)",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
              )
              Text(
                text =
                  "Inspect ${uiState.allApps.size} installed apps, see granted/denied permissions, and open each app's system Permission page directly.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
              )
            }
            Button(
              onClick = onNavigateToApps,
              colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal),
              modifier = Modifier.testTag("hub_open_app_perms_button"),
            ) {
              Text("Open Apps")
            }
          }
        }
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          OutlinedButton(
            onClick = { onLaunchTargetById("about_tv_build") },
            modifier = Modifier.weight(1f).testTag("hub_about_tv_button"),
          ) {
            Icon(
              imageVector = Icons.Filled.Info,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text("About TV (Build x7)", maxLines = 1, overflow = TextOverflow.Ellipsis)
          }
          OutlinedButton(
            onClick = { onLaunchTargetById("full_system_settings") },
            modifier = Modifier.weight(1f).testTag("hub_full_settings_button"),
          ) {
            Icon(
              imageVector = Icons.Filled.Settings,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text("Full Android Settings", maxLines = 1, overflow = TextOverflow.Ellipsis)
          }
        }
      }

      item {
        SectionHeader(
          title = "All Hidden Settings & Access",
          subtitle = "Probed live against this TV's system packages",
          icon = Icons.Filled.Build,
        )
      }

      if (isWide) {
        val rows = secondaryTargets.chunked(2)
        items(rows) { rowItems ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            rowItems.forEach { target ->
              SecondaryTargetCard(
                target = target,
                onClick = { onLaunchTarget(target) },
                modifier = Modifier.weight(1f),
              )
            }
            if (rowItems.size == 1) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
      } else {
        items(secondaryTargets, key = { it.id }) { target ->
          SecondaryTargetCard(
            target = target,
            onClick = { onLaunchTarget(target) },
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }

      item { Spacer(Modifier.height(12.dp)) }
    }
  }
}

@Composable
private fun PriorityTargetCard(
  target: HiddenSettingTarget,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val (icon, accentColor, containerColor) = categoryVisuals(target.category)

  TvFocusCard(
    onClick = onClick,
    modifier = modifier,
    testTag = "priority_target_${target.id}",
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = target.title,
          tint = accentColor,
          modifier = Modifier.padding(10.dp).size(24.dp),
        )
      }
      AvailabilityBadge(status = target.availability)
    }

    Spacer(Modifier.height(14.dp))

    Text(
      text = target.title,
      style = MaterialTheme.typography.titleLarge,
      color = TextPrimary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    Spacer(Modifier.height(4.dp))
    Text(
      text = target.subtitle,
      style = MaterialTheme.typography.bodyMedium,
      color = TextSecondary,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
    )
    Spacer(Modifier.height(12.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = target.resolvedPackage ?: "No system handler",
        style = MaterialTheme.typography.labelSmall,
        color = TextMuted,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f),
      )
      Spacer(Modifier.width(8.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "Launch",
          style = MaterialTheme.typography.labelLarge,
          color = accentColor,
        )
        Spacer(Modifier.width(4.dp))
        Icon(
          imageVector = Icons.AutoMirrored.Filled.OpenInNew,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(16.dp),
        )
      }
    }
  }
}

@Composable
private fun SecondaryTargetCard(
  target: HiddenSettingTarget,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val (icon, accentColor, containerColor) = categoryVisuals(target.category)

  TvFocusCard(
    onClick = onClick,
    modifier = modifier,
    testTag = "target_${target.id}",
    containerColor = SlateSurface,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = target.title,
          tint = accentColor,
          modifier = Modifier.padding(9.dp).size(20.dp),
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = target.title,
          style = MaterialTheme.typography.titleMedium,
          color = TextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = target.subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      AvailabilityBadge(status = target.availability)
    }
  }
}

private data class CategoryVisual(
  val icon: ImageVector,
  val accent: Color,
  val container: Color,
)

private fun categoryVisuals(category: TargetCategory): CategoryVisual {
  return when (category) {
    TargetCategory.ACCESSIBILITY ->
      CategoryVisual(Icons.Filled.AccessibilityNew, ElectricTeal, ElectricTealContainer)
    TargetCategory.DEVELOPER ->
      CategoryVisual(Icons.Filled.DeveloperMode, AmberGold, AmberGoldContainer)
    TargetCategory.USB_DEBUGGING ->
      CategoryVisual(Icons.Filled.Usb, SkyBlue, SkyBlueContainer)
    TargetCategory.APP_PERMISSIONS ->
      CategoryVisual(Icons.Filled.AdminPanelSettings, ElectricTeal, ElectricTealContainer)
    TargetCategory.SPECIAL_ACCESS ->
      CategoryVisual(Icons.Filled.Security, SkyBlue, SkyBlueContainer)
    TargetCategory.SYSTEM_INFO ->
      CategoryVisual(Icons.Filled.Settings, AmberGold, AmberGoldContainer)
  }
}
