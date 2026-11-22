package com.example.data

import android.content.Intent
import android.graphics.drawable.Drawable

enum class TargetCategory(val displayName: String) {
  ACCESSIBILITY("Accessibility"),
  DEVELOPER("Developer Options"),
  USB_DEBUGGING("USB & Wireless ADB"),
  APP_PERMISSIONS("App Permissions"),
  SPECIAL_ACCESS("Special Access"),
  SYSTEM_INFO("Device & About TV"),
}

enum class AvailabilityStatus(val label: String) {
  DIRECT_READY("Direct Ready"),
  FALLBACK_AVAILABLE("Fallback Path"),
  FIRMWARE_HIDDEN("OEM Hidden"),
}

data class IntentStrategy(
  val label: String,
  val intentProvider: () -> Intent,
)

data class HiddenSettingTarget(
  val id: String,
  val title: String,
  val subtitle: String,
  val category: TargetCategory,
  val availability: AvailabilityStatus,
  val resolvedPackage: String?,
  val adbFallbackCommand: String,
  val recoveryHint: String,
  val strategies: List<IntentStrategy>,
)

data class DeviceEnvironmentStatus(
  val deviceModel: String = "",
  val manufacturer: String = "",
  val androidVersion: String = "",
  val sdkInt: Int = 0,
  val isTvDevice: Boolean = false,
  val isDeveloperOptionsEnabled: Boolean = false,
  val isAdbEnabled: Boolean = false,
  val isAccessibilityEnabled: Boolean = false,
  val enabledAccessibilityServices: List<String> = emptyList(),
  val canDrawOverlays: Boolean = false,
  val canWriteSettings: Boolean = false,
  val isIgnoringBatteryOptimizations: Boolean = false,
  val areNotificationsEnabled: Boolean = true,
  val localIpAddress: String = "192.168.x.x",
)

data class AppPermissionEntry(
  val permissionName: String,
  val shortName: String,
  val groupLabel: String,
  val isGranted: Boolean,
  val isDangerousRuntime: Boolean,
)

data class InstalledAppInfo(
  val appName: String,
  val packageName: String,
  val versionName: String,
  val icon: Drawable?,
  val isSystemApp: Boolean,
  val isTvLeanbackApp: Boolean,
  val permissions: List<AppPermissionEntry>,
  val grantedCount: Int,
  val deniedCount: Int,
)

enum class AppFilterMode(val label: String) {
  ALL("All Apps"),
  TV_APPS("TV Apps"),
  USER_APPS("User Installed"),
  WITH_DENIED("Has Denied"),
}

sealed interface LaunchResult {
  data class Launched(val targetTitle: String, val strategyUsed: String) : LaunchResult

  data class Unavailable(val target: HiddenSettingTarget, val reason: String) : LaunchResult
}
