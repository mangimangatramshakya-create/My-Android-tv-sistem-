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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.HiddenSettingTarget
import com.example.data.TargetCategory
import com.example.ui.TvPermUiState
import com.example.ui.components.AvailabilityBadge
import com.example.ui.components.KeyStatusTile
import com.example.ui.components.SectionHeader
import com.example.ui.components.TvFocusCard
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.ElectricTealContainer
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SpecialAccessScreen(
  uiState: TvPermUiState,
  onLaunchTarget: (HiddenSettingTarget) -> Unit,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val status = uiState.deviceStatus
  val specialTargets =
    uiState.hiddenTargets.filter {
      it.category == TargetCategory.SPECIAL_ACCESS ||
        it.category == TargetCategory.ACCESSIBILITY ||
        it.id == "all_permissions_hub"
    }

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val isWide = maxWidth >= 700.dp

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          SectionHeader(
            title = "Special System Access",
            subtitle = "Live device privilege state & direct special permission toggles",
            icon = Icons.Filled.Shield,
            modifier = Modifier.weight(1f),
          )
          IconButton(
            onClick = onRefresh,
            modifier = Modifier.testTag("special_refresh_button"),
          ) {
            Icon(
              imageVector = Icons.Filled.Refresh,
              contentDescription = "Refresh special access status",
              tint = ElectricTeal,
            )
          }
        }
      }

      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            KeyStatusTile(
              label = "Accessibility Service",
              value =
                if (status.isAccessibilityEnabled)
                  "Active (${status.enabledAccessibilityServices.size} service)"
                else "Disabled",
              isOk = status.isAccessibilityEnabled,
              modifier = Modifier.weight(1f),
            )
            KeyStatusTile(
              label = "Display Over Apps",
              value = if (status.canDrawOverlays) "Allowed for Console" else "Not Granted",
              isOk = status.canDrawOverlays,
              modifier = Modifier.weight(1f),
            )
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            KeyStatusTile(
              label = "Modify Settings",
              value = if (status.canWriteSettings) "Allowed" else "Not Granted",
              isOk = status.canWriteSettings,
              modifier = Modifier.weight(1f),
            )
            KeyStatusTile(
              label = "Battery Mode",
              value =
                if (status.isIgnoringBatteryOptimizations) "Unrestricted" else "Standard",
              isOk = status.isIgnoringBatteryOptimizations,
              modifier = Modifier.weight(1f),
            )
          }
        }
      }

      if (status.enabledAccessibilityServices.isNotEmpty()) {
        item {
          Surface(
            shape = MaterialTheme.shapes.medium,
            color = SlateSurface,
            border = BorderStroke(1.dp, SlateBorder),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Filled.AccessibilityNew,
                  contentDescription = null,
                  tint = ElectricTeal,
                  modifier = Modifier.size(18.dp),
                )
                Text(
                  text = "Currently Enabled Accessibility Services",
                  style = MaterialTheme.typography.titleMedium,
                  color = TextPrimary,
                )
              }
              status.enabledAccessibilityServices.forEach { svc ->
                Text(
                  text = "• $svc",
                  style = MaterialTheme.typography.labelMedium,
                  color = ElectricTeal,
                )
              }
            }
          }
        }
      }

      item {
        SectionHeader(
          title = "Special Permission Panels",
          subtitle = "Open Android's built-in Special App Access screens directly",
          icon = Icons.Filled.AdminPanelSettings,
        )
      }

      if (isWide) {
        val rows = specialTargets.chunked(2)
        items(rows) { rowItems ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            rowItems.forEach { target ->
              SpecialTargetCard(
                target = target,
                onClick = { onLaunchTarget(target) },
                modifier = Modifier.weight(1f),
              )
            }
            if (rowItems.size == 1) {
              Spacer(Modifier.weight(1f))
            }
          }
        }
      } else {
        items(specialTargets, key = { it.id }) { target ->
          SpecialTargetCard(
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
private fun SpecialTargetCard(
  target: HiddenSettingTarget,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  TvFocusCard(
    onClick = onClick,
    modifier = modifier,
    testTag = "special_target_${target.id}",
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Surface(
        shape = MaterialTheme.shapes.small,
        color = ElectricTealContainer,
      ) {
        Icon(
          imageVector = Icons.Filled.AdminPanelSettings,
          contentDescription = null,
          tint = ElectricTeal,
          modifier = Modifier.padding(8.dp).size(20.dp),
        )
      }
      AvailabilityBadge(status = target.availability)
    }
    Spacer(Modifier.height(10.dp))
    Text(
      text = target.title,
      style = MaterialTheme.typography.titleMedium,
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
    Spacer(Modifier.height(10.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = target.resolvedPackage ?: "System Handler Check",
        style = MaterialTheme.typography.labelSmall,
        color = TextMuted,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f),
      )
      Icon(
        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
        contentDescription = "Open ${target.title}",
        tint = ElectricTeal,
        modifier = Modifier.size(16.dp),
      )
    }
  }
}
