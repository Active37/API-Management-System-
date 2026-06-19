package com.example

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.ApiKey
import com.example.data.model.Role
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
  fun greeting_screenshot() {
    val expiredKey = ApiKey(
      id = 1,
      name = "Production API Gateway Test",
      keyString = "sk_prod_expired_token_12345",
      environment = "Production",
      roleId = 1,
      isActive = true,
      createdAt = System.currentTimeMillis() - (105L * 24 * 60 * 60 * 1000) // 105 days old (exceeds 90-day threshold)
    )
    
    val mockRole = Role(
      id = 1,
      name = "Payment Processor",
      description = "Full control",
      permissions = "api:write,billing:manage"
    )

    composeTestRule.setContent { 
      MyApplicationTheme { 
        ApiKeyCard(
          apiKey = expiredKey,
          role = mockRole,
          onToggle = {},
          onDelete = {},
          onRotate = {},
          onSimulateAging = {},
          context = LocalContext.current
        )
      } 
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/expired_key_warning.png")
  }
}
