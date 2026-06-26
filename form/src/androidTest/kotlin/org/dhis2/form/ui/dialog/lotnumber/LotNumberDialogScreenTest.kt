package org.dhis2.form.ui.dialog.lotnumber

// EyeSeeTea customization - Lot Number Search Field

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import org.dhis2.form.R
import org.dhis2.form.model.lotnumber.LotNumbersResult
import org.junit.Rule
import org.junit.Test

// Tests target LotNumberDialogContent (the pure-presentation body). The surrounding
// BottomSheetShell renders in a separate ModalBottomSheet window and is validated manually.
class LotNumberDialogScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun showsSelectProductFirstMessageWhenNoProductSelected() {
        composeTestRule.setContent {
            LotNumberDialogContent(
                result = LotNumbersResult.NoProductSelected,
                searchQuery = "",
                scrollState = rememberLazyListState(),
                onLotNumberClick = {},
            )
        }

        composeTestRule.onNodeWithText(
            context.getString(R.string.lot_number_select_product_first),
        ).assertExists()
    }

    @Test
    fun showsNoLotNumbersFoundMessageWhenResultIsNotFound() {
        composeTestRule.setContent {
            LotNumberDialogContent(
                result = LotNumbersResult.NotFound,
                searchQuery = "",
                scrollState = rememberLazyListState(),
                onLotNumberClick = {},
            )
        }

        composeTestRule.onNodeWithText(
            context.getString(R.string.lot_number_none_found),
        ).assertExists()
    }

    @Test
    fun showsSelectableListAndInvokesCallbackWhenLotNumberIsClicked() {
        var selectedLotNumber: String? = null

        composeTestRule.setContent {
            LotNumberDialogContent(
                result = LotNumbersResult.Available(listOf("LOT1", "LOT2")),
                searchQuery = "",
                scrollState = rememberLazyListState(),
                onLotNumberClick = { selectedLotNumber = it },
            )
        }

        composeTestRule.onNodeWithText("LOT1").assertExists()
        composeTestRule.onNodeWithText("LOT2").assertExists()

        composeTestRule.onNodeWithText("LOT2").performClick()

        assert(selectedLotNumber == "LOT2")
    }

    @Test
    fun filtersLotNumbersBySearchQuery() {
        composeTestRule.setContent {
            LotNumberDialogContent(
                result = LotNumbersResult.Available(listOf("LOT1", "LOT2", "BATCH9")),
                searchQuery = "LOT",
                scrollState = rememberLazyListState(),
                onLotNumberClick = {},
            )
        }

        composeTestRule.onNodeWithText("LOT1").assertExists()
        composeTestRule.onNodeWithText("LOT2").assertExists()
        composeTestRule.onNodeWithText("BATCH9").assertDoesNotExist()
    }
}
