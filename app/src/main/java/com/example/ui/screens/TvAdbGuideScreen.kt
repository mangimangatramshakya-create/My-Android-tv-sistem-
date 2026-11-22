package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.TvPermUiState
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.ElectricTealContainer
import com.example.ui.theme.EmeraldOk
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TvAdbGuideScreen(
  uiState: TvPermUiState,
  onLaunchTargetById: (String) -> Unit,
  onShowMessage: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val clipboard = LocalClipboardManager.current
  val localIp = uiState.deviceStatus.localIpAddress

  val appChoices = remember(uiState.allApps) { uiState.allApps.take(12) }
  var selectedPackage by remember(appChoices) {
    mutableStateOf(appChoices.firstOrNull()?.packageName ?: "com.example.targetapp")
  }

  val permissionPresets =
    listOf(
      "Grant Camera" to "android.permission.CAMERA",
      "Grant Mic" to "android.permission.RECORD_AUDIO",
      "Grant Location" to "android.permission.ACCESS_FINE_LOCATION",
      "Grant Storage" to "android.permission.READ_EXTERNAL_STORAGE",
      "Grant Notifications" to "android.permission.POST_NOTIFICATIONS",
    )
  var selectedPerm by remember { mutableStateOf(permissionPresets.first().second) }

  val generatedCommands =
    remember(localIp, selectedPackage, selectedPerm) {
      """
      # 1. Connect wirelessly from PC or phone (ADB / Bugjaeger):
      adb connect $localIp:5555

      # 2. Grant runtime permission to selected app:
      adb shell pm grant $selectedPackage $selectedPerm

      # 3. Allow Overlay & Write Settings without UI menus:
      adb shell appops set $selectedPackage SYSTEM_ALERT_WINDOW allow
      adb shell appops set $selectedPackage WRITE_SETTINGS allow

      # 4. Enable hidden Developer Options & Accessibility:
      adb shell settings put global development_settings_enabled 1
      adb shell settings put secure accessibility_enabled 1
      """.trimIndent()
    }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      SectionHeader(
        title = "TV Unblock Guide & Wireless ADB",
        subtitle = "Workarounds when your Android TV hides Accessibility, USB Debugging, or Dev Options",
        icon = Icons.Filled.Terminal,
      )
    }

    item {
      Surface(
        shape = MaterialTheme.shapes.medium,
        color = SlateSurface,
        border = BorderStroke(1.dp, SlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Build,
              contentDescription = null,
              tint = ElectricTeal,
              modifier = Modifier.size(20.dp),
            )
            Text(
              text = "1. Manage Permissions Without Developer Options",
              style = MaterialTheme.typography.titleMedium,
              color = TextPrimary,
            )
          }
          Text(
            text =
              "You do NOT need Developer Options or USB Debugging to allow standard permissions (Storage, Mic, Camera, Overlay, Unknown Sources). Use the 'App Perms' tab in this app to pick any app and jump directly to its hidden Android Permission Controller or App Info page.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
              onClick = { onLaunchTargetById("all_permissions_hub") },
              modifier = Modifier.testTag("guide_open_perm_hub"),
            ) {
              Text("Open Permission Hub")
            }
            OutlinedButton(
              onClick = { onLaunchTargetById("accessibility") },
              modifier = Modifier.testTag("guide_open_accessibility"),
            ) {
              Text("Try Accessibility")
            }
          }
        }
      }
    }

    item {
      Surface(
        shape = MaterialTheme.shapes.medium,
        color = SlateSurface,
        border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.DeveloperMode,
              contentDescription = null,
              tint = AmberGold,
              modifier = Modifier.size(20.dp),
            )
            Text(
              text = "2. Unlock Hidden Developer Options & USB Debugging",
              style = MaterialTheme.typography.titleMedium,
              color = TextPrimary,
            )
          }
          Text(
            text =
              "Many Android TVs hide Developer Options until unlocked:\n• Tap 'Open About TV' below.\n• Scroll down to 'Android TV OS build' or 'Build'.\n• Press OK / Select on your TV remote 7 times rapidly.\n• Return to Quick Hub and tap 'Developer Options' or 'USB & Wireless Debugging'.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
              onClick = { onLaunchTargetById("about_tv_build") },
              colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
              modifier = Modifier.testTag("guide_open_about_tv"),
            ) {
              Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
              Spacer(Modifier.width(6.dp))
              Text("Open About TV (Build x7)")
            }
            OutlinedButton(
              onClick = { onLaunchTargetById("developer_options") },
              modifier = Modifier.testTag("guide_open_dev_options"),
            ) {
              Text("Open Dev Options")
            }
          }
        }
      }
    }

    item {
      Surface(
        shape = MaterialTheme.shapes.medium,
        color = SlateSurface,
        border = BorderStroke(1.dp, ElectricTeal.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Text(
            text = "3. Wireless ADB Command Builder (IP: $localIp)",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
          )
          Text(
            text =
              "If your TV firmware completely removed Accessibility or Permission UI toggles, you can grant any permission from a phone (ADB app) or laptop on the same Wi-Fi:",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
          )

          if (appChoices.isNotEmpty()) {
            Text(
              text = "Target App:",
              style = MaterialTheme.typography.labelLarge,
              color = SkyBlue,
            )
            Row(
              modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              appChoices.forEach { app ->
                FilterChip(
                  selected = selectedPackage == app.packageName,
                  onClick = { selectedPackage = app.packageName },
                  label = { Text(app.appName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                  colors =
                    FilterChipDefaults.filterChipColors(
                      selectedContainerColor = ElectricTealContainer,
                      selectedLabelColor = ElectricTeal,
                      containerColor = SlateSurfaceVariant,
                      labelColor = TextSecondary,
                    ),
                )
              }
            }
          }

          Text(
            text = "Permission Preset:",
            style = MaterialTheme.typography.labelLarge,
            color = SkyBlue,
          )
          Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            permissionPresets.forEach { (label, perm) ->
              FilterChip(
                selected = selectedPerm == perm,
                onClick = { selectedPerm = perm },
                label = { Text(label, maxLines = 1) },
                colors =
                  FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ElectricTealContainer,
                    selectedLabelColor = ElectricTeal,
                    containerColor = SlateSurfaceVariant,
                    labelColor = TextSecondary,
                  ),
              )
            }
          }

          Surface(
            shape = MaterialTheme.shapes.small,
            color = DeepNavyBg,
            border = BorderStroke(1.dp, SlateBorder),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = generatedCommands,
              style = MaterialTheme.typography.labelMedium,
              color = EmeraldOk,
              modifier = Modifier.padding(12.dp),
            )
          }

          Button(
            onClick = {
              clipboard.setText(AnnotatedString(generatedCommands))
              onShowMessage("Copied full Wireless ADB script to clipboard")
            },
            colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal),
            modifier = Modifier.align(Alignment.End).testTag("guide_copy_adb_script"),
          ) {
            Icon(
              imageVector = Icons.Filled.ContentCopy,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text("Copy Full ADB Script")
          }
        }
      }
    }

    item { Spacer(Modifier.height(12.dp)) }
  }
}
