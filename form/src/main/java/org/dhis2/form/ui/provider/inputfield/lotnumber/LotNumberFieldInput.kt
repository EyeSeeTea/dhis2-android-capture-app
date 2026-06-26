package org.dhis2.form.ui.provider.inputfield.lotnumber

// EyeSeeTea customization - Lot Number Search Field

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.dhis2.form.extensions.autocompleteList
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.dialog.lotnumber.LotNumberDialog
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.provider.onFieldFocusChanged
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.InputText

// Same values as InputShell's internal InputShellButtonSeparator + button slot
private val InputShellButtonAreaColor = Color(0xFFC5CED6) // Outline.Medium = Ash600
private val InputShellButtonAreaHeight = 48.dp
private val InputShellSeparatorHeight = 40.dp
private val InputShellRowTopPadding = 8.dp // InputShell paddingValues top = Spacing8

@Composable
fun LotNumberFieldInput(
    modifier: Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    focusManager: FocusManager,
    onNextClicked: () -> Unit,
    eventUid: String,
) {
    val textSelection = TextRange(fieldUiModel.value?.length ?: 0)
    var value by remember(fieldUiModel.value) {
        mutableStateOf(TextFieldValue(fieldUiModel.value ?: "", textSelection))
    }

    var clickedOnNext by remember { mutableStateOf(false) }
    var lostFocus by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        InputText(
            modifier = Modifier.fillMaxWidth(),
            title = fieldUiModel.label,
            state = fieldUiModel.inputState(),
            supportingText = fieldUiModel.supportingText(),
            legendData = fieldUiModel.legend(),
            inputTextFieldValue = value,
            inputStyle = inputStyle,
            isRequiredField = fieldUiModel.mandatory,
            showDeleteButton = false,
            onNextClicked = {
                clickedOnNext = true
                onNextClicked()
            },
            onValueChanged = {
                value = it ?: TextFieldValue()
                intentHandler(
                    FormIntent.OnTextChange(
                        fieldUiModel.uid,
                        value.text,
                        fieldUiModel.valueType,
                    ),
                )
            },
            onFocusChanged = { isFocused ->
                lostFocus = lostFocus == true && isFocused == false
                onFieldFocusChanged(
                    fieldUid = fieldUiModel.uid,
                    value = value.text,
                    valueType = fieldUiModel.valueType,
                    lostFocus = lostFocus,
                    onNextClicked = clickedOnNext,
                    intentHandler = intentHandler,
                )
            },
            autoCompleteList = fieldUiModel.autocompleteList(),
            onAutoCompleteItemSelected = {
                focusManager.clearFocus()
            },
        )

        // Mirrors InputShell's internal button row layout: anchored to the top of
        // the input row (top padding 8dp), 48dp tall, buttons centered vertically,
        // with a single separator drawn only between the clear and search buttons.
        Row(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = InputShellRowTopPadding)
                    .height(InputShellButtonAreaHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (value.text.isNotEmpty()) {
                Box(
                    modifier = Modifier.size(InputShellButtonAreaHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(
                        onClick = {
                            value = TextFieldValue()
                            intentHandler(
                                FormIntent.OnTextChange(
                                    fieldUiModel.uid,
                                    "",
                                    fieldUiModel.valueType,
                                ),
                            )
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Cancel,
                            contentDescription = null,
                        )
                    }
                }
                VerticalDivider(
                    color = InputShellButtonAreaColor,
                    thickness = 1.dp,
                    modifier = Modifier.height(InputShellSeparatorHeight),
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Box(
                modifier =
                    Modifier
                        .padding(end = 4.dp)
                        .size(InputShellButtonAreaHeight),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = { showDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                    )
                }
            }
        }
    }

    if (showDialog) {
        LotNumberDialog(
            eventUid = eventUid,
            onCancelClick = { showDialog = false },
            onLotNumberSelected = { lotNumber ->
                value = TextFieldValue(lotNumber, TextRange(lotNumber.length))
                intentHandler(
                    FormIntent.OnTextChange(
                        fieldUiModel.uid,
                        lotNumber,
                        fieldUiModel.valueType,
                    ),
                )
                fieldUiModel.onSave(lotNumber)
                showDialog = false
            },
        )
    }
}
