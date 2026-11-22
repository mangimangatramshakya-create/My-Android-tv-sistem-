package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppActionType
import com.example.data.AppFilterMode
import com.example.data.DeviceEnvironmentStatus
import com.example.data.HiddenSettingTarget
import com.example.data.InstalledAppInfo
import com.example.data.LaunchResult
import com.example.data.TvPermissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TvPermUiState(
  val deviceStatus: DeviceEnvironmentStatus = DeviceEnvironmentStatus(),
  val hiddenTargets: List<HiddenSettingTarget> = emptyList(),
  val allApps: List<InstalledAppInfo> = emptyList(),
  val filteredApps: List<InstalledAppInfo> = emptyList(),
  val selectedApp: InstalledAppInfo? = null,
  val searchQuery: String = "",
  val filterMode: AppFilterMode = AppFilterMode.ALL,
  val isLoadingApps: Boolean = true,
  val unavailableDialogTarget: LaunchResult.Unavailable? = null,
  val snackbarMessage: String? = null,
)

class TvPermViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = TvPermissionRepository(application.applicationContext)

  private val _uiState = MutableStateFlow(TvPermUiState())
  val uiState: StateFlow<TvPermUiState> = _uiState.asStateFlow()

  init {
    refreshAll()
  }

  fun refreshAll() {
    val status = repository.readDeviceEnvironmentStatus()
    val targets = repository.buildHiddenSettingsTargets()
    _uiState.update { state ->
      state.copy(deviceStatus = status, hiddenTargets = targets)
    }
    loadApps()
  }

  fun loadApps() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoadingApps = true) }
      val apps = repository.loadInstalledApps()
      _uiState.update { state ->
        val updatedSelected =
          state.selectedApp?.let { prev ->
            apps.firstOrNull { it.packageName == prev.packageName }
          }
        val filtered = applyFilters(apps, state.searchQuery, state.filterMode)
        state.copy(
          allApps = apps,
          filteredApps = filtered,
          selectedApp = updatedSelected,
          isLoadingApps = false,
        )
      }
    }
  }

  fun updateSearchQuery(query: String) {
    _uiState.update { state ->
      state.copy(
        searchQuery = query,
        filteredApps = applyFilters(state.allApps, query, state.filterMode),
      )
    }
  }

  fun updateFilterMode(mode: AppFilterMode) {
    _uiState.update { state ->
      state.copy(
        filterMode = mode,
        filteredApps = applyFilters(state.allApps, state.searchQuery, mode),
      )
    }
  }

  fun selectApp(app: InstalledAppInfo?) {
    _uiState.update { it.copy(selectedApp = app) }
  }

  fun launchTargetById(targetId: String) {
    val target = _uiState.value.hiddenTargets.firstOrNull { it.id == targetId } ?: return
    launchTarget(target)
  }

  fun launchTarget(target: HiddenSettingTarget) {
    when (val result = repository.launchTarget(target)) {
      is LaunchResult.Launched -> {
        _uiState.update {
          it.copy(
            snackbarMessage = "Opened ${result.targetTitle} via ${result.strategyUsed}",
            unavailableDialogTarget = null,
          )
        }
      }
      is LaunchResult.Unavailable -> {
        _uiState.update { it.copy(unavailableDialogTarget = result) }
      }
    }
  }

  fun launchAppAction(packageName: String, actionType: AppActionType) {
    when (val result = repository.launchAppAction(packageName, actionType)) {
      is LaunchResult.Launched -> {
        _uiState.update {
          it.copy(snackbarMessage = "Opened ${actionType.label} for $packageName")
        }
      }
      is LaunchResult.Unavailable -> {
        _uiState.update { it.copy(unavailableDialogTarget = result) }
      }
    }
  }

  fun dismissUnavailableDialog() {
    _uiState.update { it.copy(unavailableDialogTarget = null) }
  }

  fun clearSnackbar() {
    _uiState.update { it.copy(snackbarMessage = null) }
  }

  fun showMessage(message: String) {
    _uiState.update { it.copy(snackbarMessage = message) }
  }

  private fun applyFilters(
    apps: List<InstalledAppInfo>,
    query: String,
    mode: AppFilterMode,
  ): List<InstalledAppInfo> {
    val trimmed = query.trim().lowercase()
    return apps.filter { app ->
      val matchesQuery =
        trimmed.isEmpty() ||
          app.appName.lowercase().contains(trimmed) ||
          app.packageName.lowercase().contains(trimmed)

      val matchesMode =
        when (mode) {
          AppFilterMode.ALL -> true
          AppFilterMode.TV_APPS -> app.isTvLeanbackApp
          AppFilterMode.USER_APPS -> !app.isSystemApp
          AppFilterMode.WITH_DENIED -> app.deniedCount > 0
        }

      matchesQuery && matchesMode
    }
  }
}
