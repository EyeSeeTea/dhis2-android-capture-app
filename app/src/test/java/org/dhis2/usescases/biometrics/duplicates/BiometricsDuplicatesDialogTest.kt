package org.dhis2.usescases.biometrics.duplicates

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.kotest.matchers.shouldBe
import org.dhis2.R
import org.junit.Rule
import org.junit.Test

class BiometricsDuplicatesDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `should show both buttons when enrollNewVisible is true`() {
        var withBiometricsClicked = false
        var withoutBiometricsClicked = false

        composeTestRule.setContent {
            DialogActions(
                enrollNewVisible = true,
                enrolWithoutBiometrics = { withoutBiometricsClicked = true },
                enrolNewBiometrics = { withBiometricsClicked = true }
            )
        }

        composeTestRule.onNodeWithText("Enroll without biometrics").performClick()
        withoutBiometricsClicked shouldBe true

        composeTestRule.onNodeWithText("Enroll new").performClick()
        withBiometricsClicked shouldBe true
    }

    @Test
    fun `should show only without biometrics button when enrollNewVisible is false`() {
        var withoutBiometricsClicked = false

        composeTestRule.setContent {
            DialogActions(
                enrollNewVisible = false,
                enrolWithoutBiometrics = { withoutBiometricsClicked = true }
            )
        }

        composeTestRule.onNodeWithText("Enroll without biometrics").performClick()
        withoutBiometricsClicked shouldBe true
    }
}
