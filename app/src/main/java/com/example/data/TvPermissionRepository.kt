package com.example.data

import android.app.UiModeManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.NetworkInterface

class TvPermissionRepository(private val context: Context) {

  private val packageManager: PackageManager = context.packageManager

  fun readDeviceEnvironmentStatus(): DeviceEnvironmentStatus {
    val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
    val isTv =
      uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION ||
        packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)

    val devOptionsEnabled =
      try {
        Settings.Global.getInt(
          context.contentResolver,
          Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
          0,
        ) == 1
      } catch (_: Exception) {
        false
      }

    val adbEnabled =
      try {
        Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
      } catch (_: Exception) {
        false
      }

    val accessibilityEnabled =
      try {
        Settings.Secure.getInt(
          context.contentResolver,
          Settings.Secure.ACCESSIBILITY_ENABLED,
          0,
        ) == 1
      } catch (_: Exception) {
        false
      }

    val enabledServicesRaw =
      try {
        Settings.Secure.getString(
          context.contentResolver,
          Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ).orEmpty()
      } catch (_: Exception) {
        ""
      }

    val serviceList =
      enabledServicesRaw
        .split(':')
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    val canDrawOverlays =
      try {
        Settings.canDrawOverlays(context)
      } catch (_: Exception) {
        false
      }

    val canWriteSettings =
      try {
        Settings.System.canWrite(context)
      } catch (_: Exception) {
        false
      }

    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    val ignoringBattery =
      try {
        powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
      } catch (_: Exception) {
        false
      }

    val notificationsEnabled =
      try {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
      } catch (_: Exception) {
        true
      }

    return DeviceEnvironmentStatus(
      deviceModel = Build.MODEL ?: "Android Device",
      manufacturer = (Build.MANUFACTURER ?: "").replaceFirstChar { it.uppercase() },
      androidVersion = Build.VERSION.RELEASE ?: "Unknown",
      sdkInt = Build.VERSION.SDK_INT,
      isTvDevice = isTv,
      isDeveloperOptionsEnabled = devOptionsEnabled,
      isAdbEnabled = adbEnabled,
      isAccessibilityEnabled = accessibilityEnabled,
      enabledAccessibilityServices = serviceList,
      canDrawOverlays = canDrawOverlays,
      canWriteSettings = canWriteSettings,
      isIgnoringBatteryOptimizations = ignoringBattery,
      areNotificationsEnabled = notificationsEnabled,
      localIpAddress = resolveLocalIpv4(),
    )
  }

  fun buildHiddenSettingsTargets(): List<HiddenSettingTarget> {
    val rawTargets =
      listOf(
        RawTargetDef(
          id = "accessibility",
          title = "Accessibility Options",
          subtitle = "Open hidden TV Accessibility services, captions, and screen controls",
          category = TargetCategory.ACCESSIBILITY,
          adbFallbackCommand =
            "adb shell settings put secure accessibility_enabled 1\nadb shell am start -a android.settings.ACCESSIBILITY_SETTINGS",
          recoveryHint =
            "Some TV brands hide Accessibility in the Leanback menu. If all direct intents are blocked by firmware, use Wireless ADB or check System Settings -> Device Preferences.",
          strategies =
            listOf(
              IntentStrategy("Standard Accessibility Action") {
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
              },
              IntentStrategy("Android TV Accessibility Activity") {
                componentIntent(
                  "com.android.tv.settings",
                  "com.android.tv.settings.accessibility.AccessibilityActivity",
                )
              },
              IntentStrategy("AOSP Accessibility Dashboard") {
                componentIntent(
                  "com.android.settings",
                  "com.android.settings.Settings\$AccessibilitySettingsActivity",
                )
              },
              IntentStrategy("SubSettings Fragment Fallback") {
                subSettingsFragmentIntent("com.android.settings.accessibility.AccessibilitySettings")
              },
            ),
        ),
        RawTargetDef(
          id = "developer_options",
          title = "Developer Options",
          subtitle = "Jump straight to hidden Developer Settings without menu navigation",
          category = TargetCategory.DEVELOPER,
          adbFallbackCommand =
            "adb shell settings put global development_settings_enabled 1\nadb shell am start -a android.settings.APPLICATION_DEVELOPMENT_SETTINGS",
          recoveryHint =
            "If Developer Options is locked or hidden, open 'About TV & Build Number' below and press OK on Build Number 7 times first.",
          strategies =
            listOf(
              IntentStrategy("Standard Developer Settings") {
                Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
              },
              IntentStrategy("Android TV Development Activity") {
                componentIntent(
                  "com.android.tv.settings",
                  "com.android.tv.settings.system.development.DevelopmentActivity",
                )
              },
              IntentStrategy("AOSP Development Dashboard") {
                componentIntent(
                  "com.android.settings",
                  "com.android.settings.Settings\$DevelopmentSettingsDashboardActivity",
                )
              },
              IntentStrategy("Legacy AOSP Development") {
                componentIntent(
                  "com.android.settings",
                  "com.android.settings.DevelopmentSettings",
                )
              },
            ),
        ),
        RawTargetDef(
          id = "usb_wireless_debugging",
          title = "USB & Wireless Debugging",
          subtitle = "Access ADB debugging toggles, Wireless Debugging, and port pairing",
          category = TargetCategory.USB_DEBUGGING,
          adbFallbackCommand =
            "adb shell settings put global adb_enabled 1\nadb tcpip 5555",
          recoveryHint =
            "On Android TVs that omit USB Debugging from UI, unlocking Build Number 7x in About TV often reveals Developer Options -> Network/USB Debugging.",
          strategies =
            listOf(
              IntentStrategy("Wireless Debugging Dashboard") {
                componentIntent(
                  "com.android.settings",
                  "com.android.settings.Settings\$WirelessDebuggingActivity",
                )
              },
              IntentStrategy("TV Development & ADB Panel") {
                componentIntent(
                  "com.android.tv.settings",
                  "com.android.tv.settings.system.development.DevelopmentActivity",
                )
              },
              IntentStrategy("Standard Developer Options") {
                Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
              },
              IntentStrategy("TV System Settings Fallback") {
                componentIntent(
                  "com.android.tv.settings",
                  "com.android.tv.settings.MainSettings",
                )
              },
            ),
        ),
        RawTargetDef(
          id = "about_tv_build",
          title = "About TV (7-Tap Build Unlock)",
          subtitle = "Open Device Info to tap Build Number 7 times and unlock Dev Options",
          category = TargetCategory.SYSTEM_INFO,
          adbFallbackCommand = "adb shell am start -a android.settings.DEVICE_INFO_SETTINGS",
          recoveryHint =
            "Scroll to 'Android TV OS build' or 'Build' and press the remote's Center/OK button 7 times in a row.",
          strategies =
            listOf(
              IntentStrategy("Device Info Settings") {
                Intent(Settings.ACTION_DEVICE_INFO_SETTINGS)
              },
              IntentStrategy("Android TV About Activity") {
                componentIntent(
                  "com.android.tv.settings",
                  "com.android.tv.settings.about.AboutActivity",
                )
              },
              IntentStrategy("AOSP My Device Info") {
                componentIntent(
                  "com.android.settings",
                  "com.android.settings.Settings\$MyDeviceInfoActivity",
                )
              },
            ),
        ),
        RawTargetDef(
          id = "all_permissions_hub",
          title = "System Permission Manager",
          subtitle = "Open Android Permission Controller to allow Camera, Mic, Storage & Location",
          category = TargetCategory.APP_PERMISSIONS,
          adbFallbackCommand =
            "adb shell am start -a android.intent.action.MANAGE_PERMISSIONS",
          recoveryHint =
            "Use the 'App Perms' tab in TV PermGuard to inspect and manage permissions per app even if the global Permission Controller is hidden.",
          strategies =
            listOf(
              IntentStrategy("Android Permission Controller") {
                Intent("android.intent.action.MANAGE_PERMISSIONS")
              },
              IntentStrategy("Google Permission Controller") {
                componentIntent(
                  "com.google.android.permissioncontroller",
                  "com.android.permissioncontroller.permission.ui.ManagePermissionsActivity",
                )
              },
              IntentStrategy("AOSP Permission Controller") {
                componentIntent(
                  "com.android.permissioncontroller",
                  "com.android.permissioncontroller.permission.ui.ManagePermissionsActivity",
                )
              },
              IntentStrategy("TV Apps & Permissions List") {
                Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
              },
            ),
        ),
        RawTargetDef(
          id = "manage_all_apps",
          title = "All Apps & System Apps",
          subtitle = "Open full Applications list to manually toggle permissions for any app",
          category = TargetCategory.APP_PERMISSIONS,
          adbFallbackCommand =
            "adb shell am start -a android.settings.MANAGE_ALL_APPLICATIONS_SETTINGS",
          recoveryHint =
            "Select any app inside TV PermGuard's 'App Perms' tab for direct one-tap links.",
          strategies =
            listOf(
              IntentStrategy("Manage All Applications") {
                Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS)
              },
              IntentStrategy("Manage Applications") {
                Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
              },
              IntentStrategy("TV Apps Activity") {
                componentIntent(
                  "com.android.tv.settings",
                  "com.android.tv.settings.device.apps.AppsActivity",
                )
              },
            ),
        ),
        RawTargetDef(
          id = "overlay_permission",
          title = "Display Over Other Apps",
          subtitle = "Grant SYSTEM_ALERT_WINDOW / floating overlay access on TV",
          category = TargetCategory.SPECIAL_ACCESS,
          adbFallbackCommand =
            "adb shell appops set ${context.packageName} SYSTEM_ALERT_WINDOW allow",
          recoveryHint =
            "Required by floating mouse pointers, sidebars, and screen-dimmer apps on Android TV.",
          strategies =
            listOf(
              IntentStrategy("Manage Overlay Settings") {
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
              },
              IntentStrategy("App-Scoped Overlay Screen") {
                Intent(
                  Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                  Uri.parse("package:${context.packageName}"),
                )
              },
              IntentStrategy("AOSP Overlay List") {
                componentIntent(
                  "com.android.settings",
                  "com.android.settings.Settings\$OverlaySettingsActivity",
                )
              },
            ),
        ),
        RawTargetDef(
          id = "write_settings",
          title = "Modify System Settings",
          subtitle = "Allow apps to change brightness, rotation, and TV system values",
          category = TargetCategory.SPECIAL_ACCESS,
          adbFallbackCommand =
            "adb shell appops set ${context.packageName} WRITE_SETTINGS allow",
          recoveryHint = "Select the target app in the list to toggle 'Allow modifying system settings'.",
          strategies =
            listOf(
              IntentStrategy("Manage Write Settings") {
                Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
              },
              IntentStrategy("App-Scoped Write Settings") {
                Intent(
                  Settings.ACTION_MANAGE_WRITE_SETTINGS,
                  Uri.parse("package:${context.packageName}"),
                )
              },
            ),
        ),
        RawTargetDef(
          id = "unknown_sources",
          title = "Install Unknown Apps",
          subtitle = "Enable sideloading permissions per file manager or browser on TV",
          category = TargetCategory.SPECIAL_ACCESS,
          adbFallbackCommand =
            "adb shell appops set <package_name> REQUEST_INSTALL_PACKAGES allow",
          recoveryHint =
            "On Android 8+, open 'App Perms', select your File Manager/Browser, and tap 'Install Unknown Apps'.",
          strategies =
            buildList {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                add(
                  IntentStrategy("Manage Unknown App Sources") {
                    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                  }
                )
              }
              add(
                IntentStrategy("Security & Unknown Sources") {
                  Intent(Settings.ACTION_SECURITY_SETTINGS)
                }
              )
              add(
                IntentStrategy("TV Security Settings") {
                  componentIntent(
                    "com.android.tv.settings",
                    "com.android.tv.settings.system.SecurityActivity",
                  )
                }
              )
            },
        ),
        RawTargetDef(
          id = "usage_access",
          title = "Usage Data Access",
          subtitle = "Grant app usage tracking needed by custom TV launchers & button mappers",
          category = TargetCategory.SPECIAL_ACCESS,
          adbFallbackCommand =
            "adb shell appops set <package_name> GET_USAGE_STATS allow",
          recoveryHint = "Some TV boxes hide Usage Access; use ADB appops if the menu is omitted.",
          strategies =
            listOf(
              IntentStrategy("Usage Access Settings") {
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
              },
              IntentStrategy("AOSP Usage Access") {
                componentIntent(
                  "com.android.settings",
                  "com.android.settings.Settings\$UsageAccessSettingsActivity",
                )
              },
            ),
        ),
        RawTargetDef(
          id = "notification_access",
          title = "Notification Listener Access",
          subtitle = "Manage apps allowed to read TV notifications and media alerts",
          category = TargetCategory.SPECIAL_ACCESS,
          adbFallbackCommand =
            "adb shell cmd notification allow_listener <pkg>/<service>",
          recoveryHint = "Check Special Access -> Notification Access if supported by your TV build.",
          strategies =
            listOf(
              IntentStrategy("Notification Listener Settings") {
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
              },
            ),
        ),
        RawTargetDef(
          id = "battery_optimization",
          title = "Battery & Background Access",
          subtitle = "Keep background TV utilities and remappers alive without OS throttling",
          category = TargetCategory.SPECIAL_ACCESS,
          adbFallbackCommand =
            "adb shell dumpsys deviceidle whitelist +<package_name>",
          recoveryHint = "Set target apps to 'Unrestricted' or 'Don't optimize'.",
          strategies =
            listOf(
              IntentStrategy("Ignore Battery Optimization List") {
                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
              },
            ),
        ),
        RawTargetDef(
          id = "full_system_settings",
          title = "Full Android Settings",
          subtitle = "Open hidden stock Android Settings panel instead of simplified TV menu",
          category = TargetCategory.SYSTEM_INFO,
          adbFallbackCommand = "adb shell am start -a android.settings.SETTINGS",
          recoveryHint = "Opens the system Settings container available on this TV.",
          strategies =
            listOf(
              IntentStrategy("AOSP Settings Activity") {
                componentIntent("com.android.settings", "com.android.settings.Settings")
              },
              IntentStrategy("Standard System Settings") {
                Intent(Settings.ACTION_SETTINGS)
              },
              IntentStrategy("TV Main Settings") {
                componentIntent(
                  "com.android.tv.settings",
                  "com.android.tv.settings.MainSettings",
                )
              },
            ),
        ),
      )

    return rawTargets.map { raw ->
      var availability = AvailabilityStatus.FIRMWARE_HIDDEN
      var resolvedPkg: String? = null
      for ((index, strategy) in raw.strategies.withIndex()) {
        val intent = strategy.intentProvider()
        val resolved =
          try {
            packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
          } catch (_: Exception) {
            null
          }
        if (resolved != null) {
          resolvedPkg = resolved.activityInfo?.packageName
          availability =
            if (index == 0) AvailabilityStatus.DIRECT_READY
            else AvailabilityStatus.FALLBACK_AVAILABLE
          break
        }
      }

      HiddenSettingTarget(
        id = raw.id,
        title = raw.title,
        subtitle = raw.subtitle,
        category = raw.category,
        availability = availability,
        resolvedPackage = resolvedPkg,
        adbFallbackCommand = raw.adbFallbackCommand,
        recoveryHint = raw.recoveryHint,
        strategies = raw.strategies,
      )
    }
  }

  fun launchTarget(target: HiddenSettingTarget): LaunchResult {
    for (strategy in target.strategies) {
      val intent =
        strategy.intentProvider().apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
      val canResolve =
        try {
          packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
        } catch (_: Exception) {
          false
        }
      if (canResolve) {
        try {
          context.startActivity(intent)
          return LaunchResult.Launched(target.title, strategy.label)
        } catch (_: SecurityException) {
          // Try next fallback strategy if OEM restricted direct access to this component
        } catch (_: Exception) {
          // Try next fallback strategy
        }
      }
    }
    return LaunchResult.Unavailable(
      target = target,
      reason =
        "This Android TV firmware blocked or removed all known system activities for '${target.title}'.",
    )
  }

  fun launchAppAction(packageName: String, actionType: AppActionType): LaunchResult {
    val strategies: List<IntentStrategy> =
      when (actionType) {
        AppActionType.MANAGE_PERMISSIONS ->
          listOf(
            IntentStrategy("Permission Controller App Screen") {
              Intent("android.intent.action.MANAGE_APP_PERMISSIONS").apply {
                putExtra(Intent.EXTRA_PACKAGE_NAME, packageName)
              }
            },
            IntentStrategy("App Details Settings") {
              Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null),
              )
            },
          )

        AppActionType.APP_DETAILS ->
          listOf(
            IntentStrategy("App Info Screen") {
              Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null),
              )
            }
          )

        AppActionType.OVERLAY ->
          listOf(
            IntentStrategy("App Overlay Permission") {
              Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.fromParts("package", packageName, null),
              )
            },
            IntentStrategy("Global Overlay Settings") {
              Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            },
          )

        AppActionType.WRITE_SETTINGS ->
          listOf(
            IntentStrategy("App Write Settings") {
              Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.fromParts("package", packageName, null),
              )
            },
            IntentStrategy("Global Write Settings") {
              Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            },
          )

        AppActionType.UNKNOWN_SOURCES ->
          buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              add(
                IntentStrategy("App Install Unknown Sources") {
                  Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.fromParts("package", packageName, null),
                  )
                }
              )
            }
            add(
              IntentStrategy("App Details Fallback") {
                Intent(
                  Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                  Uri.fromParts("package", packageName, null),
                )
              }
            )
          }

        AppActionType.NOTIFICATIONS ->
          buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              add(
                IntentStrategy("App Notification Settings") {
                  Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                  }
                }
              )
            }
            add(
              IntentStrategy("App Details Fallback") {
                Intent(
                  Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                  Uri.fromParts("package", packageName, null),
                )
              }
            )
          }

        AppActionType.ALL_FILES ->
          buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
              add(
                IntentStrategy("All Files Access") {
                  Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.fromParts("package", packageName, null),
                  )
                }
              )
            }
            add(
              IntentStrategy("App Details Fallback") {
                Intent(
                  Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                  Uri.fromParts("package", packageName, null),
                )
              }
            )
          }
      }

    val tempTarget =
      HiddenSettingTarget(
        id = "${actionType.name}_$packageName",
        title = "${actionType.label} ($packageName)",
        subtitle = "",
        category = TargetCategory.APP_PERMISSIONS,
        availability = AvailabilityStatus.DIRECT_READY,
        resolvedPackage = null,
        adbFallbackCommand =
          "adb shell am start -a android.settings.APPLICATION_DETAILS_SETTINGS -d package:$packageName",
        recoveryHint =
          "If this TV hides individual permission tabs, try opening 'App Info' or granting via Wireless ADB.",
        strategies = strategies,
      )
    return launchTarget(tempTarget)
  }

  suspend fun loadInstalledApps(): List<InstalledAppInfo> =
    withContext(Dispatchers.IO) {
      val launcherIntent =
        Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
      val leanbackIntent =
        Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER) }

      val launcherResolved =
        try {
          packageManager.queryIntentActivities(launcherIntent, 0)
        } catch (_: Exception) {
          emptyList()
        }

      val leanbackResolved =
        try {
          packageManager.queryIntentActivities(leanbackIntent, 0)
        } catch (_: Exception) {
          emptyList()
        }

      val leanbackPackages = leanbackResolved.mapNotNull { it.activityInfo?.packageName }.toSet()
      val uniquePackages =
        (launcherResolved + leanbackResolved)
          .mapNotNull { it.activityInfo?.packageName }
          .toSet()

      uniquePackages
        .mapNotNull { pkgName -> buildAppInfo(pkgName, leanbackPackages.contains(pkgName)) }
        .sortedBy { it.appName.lowercase() }
    }

  private fun buildAppInfo(packageName: String, isTvLeanback: Boolean): InstalledAppInfo? {
    return try {
      val pkgInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          packageManager.getPackageInfo(
            packageName,
            PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()),
          )
        } else {
          @Suppress("DEPRECATION")
          packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        }

      val appInfo = pkgInfo.applicationInfo ?: return null
      val label = packageManager.getApplicationLabel(appInfo)?.toString().orEmpty().ifBlank {
        packageName
      }
      val icon =
        try {
          packageManager.getApplicationIcon(appInfo)
        } catch (_: Exception) {
          null
        }
      val isSystem =
        (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
          (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

      val reqPerms = pkgInfo.requestedPermissions ?: emptyArray()
      val reqFlags = pkgInfo.requestedPermissionsFlags ?: IntArray(0)

      val entries =
        reqPerms.mapIndexed { idx, perm ->
          val granted =
            idx < reqFlags.size &&
              (reqFlags[idx] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
          val shortName = perm.substringAfterLast('.')
          AppPermissionEntry(
            permissionName = perm,
            shortName = shortName,
            groupLabel = classifyPermissionGroup(perm),
            isGranted = granted,
            isDangerousRuntime = isCommonRuntimeOrSpecialPermission(perm),
          )
        }
          .sortedWith(
            compareByDescending<AppPermissionEntry> { it.isDangerousRuntime }
              .thenBy { it.isGranted }
              .thenBy { it.shortName }
          )

      InstalledAppInfo(
        appName = label,
        packageName = packageName,
        versionName = pkgInfo.versionName ?: "1.0",
        icon = icon,
        isSystemApp = isSystem,
        isTvLeanbackApp = isTvLeanback,
        permissions = entries,
        grantedCount = entries.count { it.isGranted },
        deniedCount = entries.count { !it.isGranted },
      )
    } catch (_: Exception) {
      null
    }
  }

  private fun classifyPermissionGroup(permission: String): String {
    return when {
      permission.contains("CAMERA") -> "Camera"
      permission.contains("RECORD_AUDIO") || permission.contains("MICROPHONE") -> "Microphone"
      permission.contains("LOCATION") -> "Location"
      permission.contains("STORAGE") ||
        permission.contains("MEDIA") ||
        permission.contains("FILES") -> "Files & Media"
      permission.contains("BLUETOOTH") || permission.contains("NEARBY") -> "Nearby / Bluetooth"
      permission.contains("NOTIFICATION") -> "Notifications"
      permission.contains("OVERLAY") || permission.contains("SYSTEM_ALERT_WINDOW") -> "Overlay"
      permission.contains("WRITE_SETTINGS") || permission.contains("PACKAGE") -> "System / Install"
      permission.contains("INTERNET") ||
        permission.contains("NETWORK") ||
        permission.contains("WIFI") -> "Network"
      else -> "Other"
    }
  }

  private fun isCommonRuntimeOrSpecialPermission(permission: String): Boolean {
    val interesting =
      listOf(
        "CAMERA",
        "RECORD_AUDIO",
        "ACCESS_FINE_LOCATION",
        "ACCESS_COARSE_LOCATION",
        "ACCESS_BACKGROUND_LOCATION",
        "READ_EXTERNAL_STORAGE",
        "WRITE_EXTERNAL_STORAGE",
        "MANAGE_EXTERNAL_STORAGE",
        "READ_MEDIA_IMAGES",
        "READ_MEDIA_VIDEO",
        "READ_MEDIA_AUDIO",
        "POST_NOTIFICATIONS",
        "BLUETOOTH_CONNECT",
        "BLUETOOTH_SCAN",
        "SYSTEM_ALERT_WINDOW",
        "WRITE_SETTINGS",
        "REQUEST_INSTALL_PACKAGES",
        "PACKAGE_USAGE_STATS",
        "READ_CONTACTS",
        "READ_PHONE_STATE",
      )
    return interesting.any { permission.endsWith(it) }
  }

  private fun componentIntent(pkg: String, cls: String): Intent {
    return Intent().apply { component = ComponentName(pkg, cls) }
  }

  private fun subSettingsFragmentIntent(fragmentName: String): Intent {
    return Intent().apply {
      component = ComponentName("com.android.settings", "com.android.settings.SubSettings")
      putExtra(":settings:show_fragment", fragmentName)
    }
  }

  private fun resolveLocalIpv4(): String {
    return try {
      val interfaces = NetworkInterface.getNetworkInterfaces() ?: return "192.168.1.x"
      while (interfaces.hasMoreElements()) {
        val iface = interfaces.nextElement()
        if (!iface.isUp || iface.isLoopback) continue
        val addrs = iface.inetAddresses
        while (addrs.hasMoreElements()) {
          val addr = addrs.nextElement()
          if (addr is Inet4Address && !addr.isLoopbackAddress) {
            return addr.hostAddress ?: "192.168.1.x"
          }
        }
      }
      "192.168.1.x"
    } catch (_: Exception) {
      "192.168.1.x"
    }
  }
}

enum class AppActionType(val label: String) {
  MANAGE_PERMISSIONS("Manage Permissions"),
  APP_DETAILS("App Info"),
  OVERLAY("Display Over Apps"),
  WRITE_SETTINGS("Modify Settings"),
  UNKNOWN_SOURCES("Unknown Sources"),
  NOTIFICATIONS("Notifications"),
  ALL_FILES("All Files Access"),
}

private data class RawTargetDef(
  val id: String,
  val title: String,
  val subtitle: String,
  val category: TargetCategory,
  val adbFallbackCommand: String,
  val recoveryHint: String,
  val strategies: List<IntentStrategy>,
)
