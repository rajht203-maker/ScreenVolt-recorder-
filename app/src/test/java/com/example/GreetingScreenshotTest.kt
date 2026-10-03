package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.RecordingSettings
import com.example.service.RecorderState
import com.example.ui.StorageInfo
import com.example.ui.screens.StudioStatusCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun studio_status_card_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        StudioStatusCard(
          storageInfo = StorageInfo(32_000_000_000L, 64_000_000_000L, 480),
          recorderState = RecorderState.Idle,
          settings = RecordingSettings()
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/studio_status.png")
  }
}
