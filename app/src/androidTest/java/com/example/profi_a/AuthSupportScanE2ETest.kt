package com.example.profi_a

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.profia.app.MainActivity

/**
 * E2E instrumented tests for critical flows: Auth, Support (RoomScan — see docs/SCAN_SERVER_AND_DEVICE_TESTS.md).
 * See docs/E2E_TEST_CASES.md. Requires device/emulator; app starts at Splash. Locale: RU.
 */
@RunWith(AndroidJUnit4::class)
class AuthSupportScanE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun authScreen_opensAfterSplashAuthorizeClick() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Авторизоваться").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("auth_screen").assertIsDisplayed()
    }

    @Test
    fun supportScreen_opensFromDrawerAfterDemoMode() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Режим демо на 3 дня").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Меню").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Тех. поддержка").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("support_screen").assertIsDisplayed()
    }
}
