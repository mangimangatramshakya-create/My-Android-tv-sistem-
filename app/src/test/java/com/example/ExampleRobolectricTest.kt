package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.TvPermissionRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TV PermGuard", appName)
  }

  @Test
  fun `repository builds essential TV permission and hidden settings targets`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = TvPermissionRepository(context)
    val targets = repo.buildHiddenSettingsTargets()
    val ids = targets.map { it.id }.toSet()

    assertTrue(ids.contains("accessibility"))
    assertTrue(ids.contains("developer_options"))
    assertTrue(ids.contains("usb_wireless_debugging"))
    assertTrue(ids.contains("all_permissions_hub"))
    assertTrue(ids.contains("about_tv_build"))

    val status = repo.readDeviceEnvironmentStatus()
    assertTrue(status.sdkInt > 0)
  }
}
