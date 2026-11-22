package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AvailabilityStatus
import com.example.data.DeviceEnvironmentStatus
import com.example.data.LaunchResult
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldContainer
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.CoralAlertContainer
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.ElectricTealContainer
import com.example.ui.theme.EmeraldOk
import com.example.ui.theme.EmeraldOkContainer
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueContainer
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TvFocusCard(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String? = null,
  selected: Boolean = false,
  containerColor: Color = SlateSurface,
  content: @Composable ColumnScope.() -> Unit,
) {
  var isFocused by remember { mutableStateOf(false) }
  val interactionSource = remember { MutableInteractionSource() }

  val scale by
    animateFloatAsState(
      targetValue = if (isFocused) 1.02f else 1f,
      animationSpec = spring(),
      label = "tvCardScale",
    )

  val borderColor by
    animateColorAsState(
      targetValue =
        when {
          isFocused -> ElectricTeal
          selected -> SkyBlue
          else -> SlateBorder
        },
      label = "tvCardBorder",
    )

  val resolvedBg by
    animateColorAsState(
      targetValue =
        when {
          isFocused -> SlateSurfaceVariant
          selected -> SkyBlueContainer.copy(alpha = 0.45f)
          else -> containerColor
        },
      label = "tvCardBg",
    )

  Surface(
    modifier =
      modifier
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
        .graphicsLayer {
          scaleX = scale
          scaleY = scale
        }
        .minimumInteractiveComponentSize()
        .onFocusChanged { isFocused = it.isFocused }
        .focusable()
        .clip(MaterialTheme.shapes.medium)
        .clickable(
          interactionSource = interactionSource,
          indication = androidx.compose.material3.ripple(),
          onClick = onClick,
        ),
    shape = MaterialTheme.shapes.medium,
    color = resolvedBg,
    border = BorderStroke(if (isFocused || selected) 2.dp else 1.dp, borderColor),
    tonalElevation = if (isFocused) 6.dp else 2.dp,
  ) {
    Column(modifier = Modifier.padding(16.dp), content = content)
  }
}

@Composable
fun StatusPill(
  text: String,
  containerColor: Color,
  contentColor: Color,
  modifier: Modifier = Modifier,
  showDot: Boolean = true,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(999.dp),
    color = containerColor,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      if (showDot) {
        Box(
          modifier =
            Modifier
              .size(7.dp)
              .clip(CircleShape)
              .background(contentColor)
        )
      }
      Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = contentColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
fun AvailabilityBadge(status: AvailabilityStatus) {
  when (status) {
    AvailabilityStatus.DIRECT_READY ->
      StatusPill(
        text = status.label,
        containerColor = EmeraldOkContainer,
        contentColor = EmeraldOk,
      )
    AvailabilityStatus.FALLBACK_AVAILABLE ->
      StatusPill(
        text = status.label,
        containerColor = SkyBlueContainer,
        contentColor = SkyBlue,
      )
    AvailabilityStatus.FIRMWARE_HIDDEN ->
      StatusPill(
        text = status.label,
        containerColor = AmberGoldContainer,
        contentColor = AmberGold,
      )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroConsoleBanner(
  deviceStatus: DeviceEnvironmentStatus,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    border = BorderStroke(1.dp, SlateBorder),
    color = SlateSurface,
  ) {
    Box(modifier = Modifier.fillMaxWidth()) {
      Image(
        painter = painterResource(id = R.drawable.img_tv_hero_1795311170400),
        contentDescription = "Android TV permission console header",
        modifier = Modifier.matchParentSize(),
        contentScale = ContentScale.Crop,
      )
      Box(
        modifier =
          Modifier
            .matchParentSize()
            .background(
              Brush.horizontalGradient(
                colors =
                  listOf(
                    DeepNavyBg.copy(alpha = 0.96f),
                    DeepNavyBg.copy(alpha = 0.85f),
                    DeepNavyBg.copy(alpha = 0.50f),
                  )
              )
            )
      )
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = ElectricTealContainer,
            border = BorderStroke(1.dp, ElectricTeal.copy(alpha = 0.5f)),
          ) {
            Icon(
              imageVector = Icons.Filled.Tv,
              contentDescription = null,
              tint = ElectricTeal,
              modifier = Modifier.padding(8.dp).size(22.dp),
            )
          }
          Column {
            Text(
              text = stringResource(R.string.hero_title),
              style = MaterialTheme.typography.headlineMedium,
              color = TextPrimary,
            )
            Text(
              text =
                "${deviceStatus.manufacturer} ${deviceStatus.deviceModel} • Android ${deviceStatus.androidVersion} (API ${deviceStatus.sdkInt})",
              style = MaterialTheme.typography.labelMedium,
              color = ElectricTeal,
            )
          }
        }

        Text(
          text = stringResource(R.string.hero_subtitle),
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          StatusPill(
            text = if (deviceStatus.isTvDevice) "TV Leanback Mode" else "Handheld / Emulator",
            containerColor = SkyBlueContainer,
            contentColor = SkyBlue,
          )
          StatusPill(
            text =
              if (deviceStatus.isAccessibilityEnabled) "Accessibility: ON"
              else "Accessibility: OFF",
            containerColor =
              if (deviceStatus.isAccessibilityEnabled) EmeraldOkContainer else CoralAlertContainer,
            contentColor = if (deviceStatus.isAccessibilityEnabled) EmeraldOk else CoralAlert,
          )
          StatusPill(
            text =
              if (deviceStatus.isDeveloperOptionsEnabled) "Dev Options: ON"
              else "Dev Options: Hidden/OFF",
            containerColor =
              if (deviceStatus.isDeveloperOptionsEnabled) EmeraldOkContainer else AmberGoldContainer,
            contentColor = if (deviceStatus.isDeveloperOptionsEnabled) EmeraldOk else AmberGold,
          )
          StatusPill(
            text = if (deviceStatus.isAdbEnabled) "USB/ADB: ON" else "USB/ADB: OFF",
            containerColor =
              if (deviceStatus.isAdbEnabled) EmeraldOkContainer else SlateSurfaceVariant,
            contentColor = if (deviceStatus.isAdbEnabled) EmeraldOk else TextSecondary,
          )
        }
      }
    }
  }
}

@Composable
fun SectionHeader(
  title: String,
  subtitle: String? = null,
  icon: ImageVector? = null,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    if (icon != null) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = ElectricTeal,
        modifier = Modifier.size(20.dp),
      )
    }
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = TextPrimary,
      )
      if (!subtitle.isNullOrBlank()) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary,
        )
      }
    }
  }
}

@Composable
fun DrawableAppIcon(
  drawable: Drawable?,
  contentDescription: String,
  modifier: Modifier = Modifier,
) {
  val bitmap =
    remember(drawable) {
      drawable?.let { d ->
        if (d is BitmapDrawable && d.bitmap != null) {
          d.bitmap
        } else {
          val w = d.intrinsicWidth.takeIf { it > 0 } ?: 96
          val h = d.intrinsicHeight.takeIf { it > 0 } ?: 96
          val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
          val canvas = Canvas(bmp)
          d.setBounds(0, 0, canvas.width, canvas.height)
          d.draw(canvas)
          bmp
        }
      }
    }

  if (bitmap != null) {
    Image(
      bitmap = bitmap.asImageBitmap(),
      contentDescription = contentDescription,
      modifier = modifier.clip(RoundedCornerShape(12.dp)),
      contentScale = ContentScale.Fit,
    )
  } else {
    Surface(
      modifier = modifier,
      shape = RoundedCornerShape(12.dp),
      color = SlateSurfaceVariant,
    ) {
      Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Icon(
          imageVector = Icons.Filled.Settings,
          contentDescription = contentDescription,
          tint = ElectricTeal,
          modifier = Modifier.size(22.dp),
        )
      }
    }
  }
}

@Composable
fun FallbackDiagnosticsDialog(
  unavailable: LaunchResult.Unavailable,
  localIp: String,
  onOpenAboutTv: () -> Unit,
  onOpenFullSettings: () -> Unit,
  onCopiedMessage: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val clipboard = LocalClipboardManager.current
  val adbText =
    remember(unavailable, localIp) {
      "adb connect $localIp:5555\n${unavailable.target.adbFallbackCommand}"
    }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = SlateSurface,
    titleContentColor = TextPrimary,
    textContentColor = TextSecondary,
    icon = {
      Icon(
        imageVector = Icons.Filled.Warning,
        contentDescription = null,
        tint = AmberGold,
        modifier = Modifier.size(30.dp),
      )
    },
    title = {
      Text(
        text = "System Screen Blocked on This TV",
        style = MaterialTheme.typography.titleLarge,
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = unavailable.reason,
          style = MaterialTheme.typography.bodyMedium,
          color = TextPrimary,
        )
        Surface(
          shape = MaterialTheme.shapes.small,
          color = SlateSurfaceVariant,
          border = BorderStroke(1.dp, SlateBorder),
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
          ) {
            Icon(
              imageVector = Icons.Filled.Info,
              contentDescription = null,
              tint = SkyBlue,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = unavailable.target.recoveryHint,
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
            )
          }
        }
        Text(
          text = "Wireless ADB Fallback Command:",
          style = MaterialTheme.typography.labelLarge,
          color = ElectricTeal,
        )
        Surface(
          shape = MaterialTheme.shapes.small,
          color = DeepNavyBg,
          border = BorderStroke(1.dp, SlateBorder),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = adbText,
            style = MaterialTheme.typography.labelMedium,
            color = EmeraldOk,
            modifier = Modifier.padding(10.dp),
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedButton(
            onClick = {
              onDismiss()
              onOpenAboutTv()
            },
            modifier = Modifier.weight(1f).testTag("dialog_open_about_tv"),
          ) {
            Text("About TV (7x)", maxLines = 1)
          }
          OutlinedButton(
            onClick = {
              onDismiss()
              onOpenFullSettings()
            },
            modifier = Modifier.weight(1f).testTag("dialog_open_full_settings"),
          ) {
            Text("Full Settings", maxLines = 1)
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          clipboard.setText(AnnotatedString(adbText))
          onCopiedMessage("Copied ADB command to clipboard")
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal),
        modifier = Modifier.testTag("dialog_copy_adb_button"),
      ) {
        Icon(
          imageVector = Icons.Filled.ContentCopy,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text("Copy ADB")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("dialog_close_button"),
      ) {
        Text("Close")
      }
    },
  )
}

@Composable
fun KeyStatusTile(
  label: String,
  value: String,
  isOk: Boolean,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = MaterialTheme.shapes.medium,
    color = SlateSurface,
    border = BorderStroke(1.dp, if (isOk) EmeraldOk.copy(alpha = 0.4f) else SlateBorder),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
        imageVector = if (isOk) Icons.Filled.CheckCircle else Icons.Filled.Warning,
        contentDescription = null,
        tint = if (isOk) EmeraldOk else AmberGold,
        modifier = Modifier.size(20.dp),
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelLarge,
          color = TextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = value,
          style = MaterialTheme.typography.labelMedium,
          color = if (isOk) EmeraldOk else TextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}
