package org.dhis2.form.ui.dialog.lotnumber

// EyeSeeTea customization - Lot Number Search Field

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.dhis2.form.R
import org.dhis2.form.model.lotnumber.LotNumbersResult
import org.hisp.dhis.mobile.ui.designsystem.component.BottomSheetShell
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownItem
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownListItem
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellUIState
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing.Spacing8

@Composable
fun LotNumberDialogScreen(
    result: LotNumbersResult?,
    onCancelClick: () -> Unit,
    onLotNumberClick: (lotNumber: String) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val scrollState = rememberLazyListState()

    // Only show the search bar once we have a list to filter.
    val showSearch = result is LotNumbersResult.Available

    BottomSheetShell(
        uiState =
            BottomSheetShellUIState(
                title = stringResource(id = R.string.lot_number_select_title),
                searchQuery = if (showSearch) searchQuery else null,
                showTopSectionDivider = true,
                showBottomSectionDivider = true,
            ),
        contentScrollState = scrollState,
        onSearchQueryChanged = { searchQuery = it },
        onSearch = { searchQuery = it },
        onDismiss = onCancelClick,
        content = {
            LotNumberDialogContent(
                result = result,
                searchQuery = searchQuery,
                scrollState = scrollState,
                onLotNumberClick = onLotNumberClick,
            )
        },
    )
}

/**
 * Pure-presentation content of the lot-number selector, decoupled from [BottomSheetShell] so it
 * can be exercised in isolation by Compose tests (the shell renders in a separate ModalBottomSheet
 * window that is awkward to test directly).
 */
@Composable
internal fun LotNumberDialogContent(
    result: LotNumbersResult?,
    searchQuery: String,
    scrollState: LazyListState,
    onLotNumberClick: (lotNumber: String) -> Unit,
) {
    when (result) {
        null ->
            CenteredMessage {
                ProgressIndicator(type = ProgressIndicatorType.CIRCULAR_SMALL)
            }

        is LotNumbersResult.NoProductSelected ->
            CenteredMessage {
                MessageText(stringResource(id = R.string.lot_number_select_product_first))
            }

        is LotNumbersResult.NotFound ->
            CenteredMessage {
                MessageText(stringResource(id = R.string.lot_number_none_found))
            }

        is LotNumbersResult.Available -> {
            val filtered =
                remember(result.lotNumbers, searchQuery) {
                    if (searchQuery.isBlank()) {
                        result.lotNumbers
                    } else {
                        result.lotNumbers.filter { it.contains(searchQuery, ignoreCase = true) }
                    }
                }
            LotNumberList(
                lotNumbers = filtered,
                scrollState = scrollState,
                onLotNumberClick = onLotNumberClick,
            )
        }
    }
}

@Composable
private fun CenteredMessage(content: @Composable () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun MessageText(message: String) {
    Text(
        text = message,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
private fun LotNumberList(
    lotNumbers: List<String>,
    scrollState: LazyListState,
    onLotNumberClick: (lotNumber: String) -> Unit,
) {
    LazyColumn(state = scrollState) {
        items(items = lotNumbers) { lotNumber ->
            DropdownListItem(
                item = DropdownItem(lotNumber),
                contentPadding = PaddingValues(Spacing8),
                onItemClick = { onLotNumberClick(lotNumber) },
            )
        }
    }
}
