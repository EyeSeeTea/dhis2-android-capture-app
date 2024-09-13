package org.dhis2.usescases.biometrics.ui.confirmationDialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.ui.model.ListCardUiModel
import org.dhis2.usescases.searchTrackEntity.ui.mapper.TEICardMapper

const val BIOMETRICS_SEARCH_CONFIRMATION_DIALOG_TAG: String =
    "BIOMETRICS_SEARCH_CONFIRMATION_DIALOG_TAG"

class BiometricsSearchConfirmationDialog(
    private val item: SearchTeiModel, private val cardMapper: TEICardMapper,
    private val cancelCallback: () -> Unit,
    private val confirmCallback: () -> Unit,
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawableResource(android.R.color.transparent)
            setWindowAnimations(R.style.pin_dialog_animation)
            isCancelable = false
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val card = cardMapper.mapForConfirmationDialog(
            searchTEIModel = item,
        )

        return ComposeView(requireContext()).apply {
            setContent {
                ConfirmContent(card, {
                    cancelCallback()
                    dismiss()
                }, {
                    confirmCallback()
                    dismiss()
                })
            }
        }
    }

    override fun dismiss() {
        app().releaseSessionComponent()
        dismissAllowingStateLoss()
    }


    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.findFragmentByTag(tag) == null) {
            manager.beginTransaction().add(this, tag).commitAllowingStateLoss()
        }
    }
}

@Composable
private fun ConfirmContent(
    card: ListCardUiModel,
    cancelAction: () -> Unit,
    confirmAction: () -> Unit
) {
    Surface(color = Color.White) {
        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.biometrics_confirm_patient_identity),
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(color = Color(0xFFfafafa))
                    .border(1.dp, Color.Gray)
                    .padding(16.dp)
            ) {
                /*ListCard(
                    listAvatar = card.avatar,
                    title = ListCardTitleModel(text = card.title),
                    lastUpdated = card.lastUpdated,
                    additionalInfoList = card.additionalInfo,
                    actionButton = card.actionButton,
                    expandLabelText = card.expandLabelText,
                    shrinkLabelText = card.shrinkLabelText,
                    onCardClick = card.onCardCLick,
                )*/
                TEIContent(card)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = cancelAction
                ) {
                    Text(
                        stringResource(R.string.cancel).uppercase(),
                        color = Color(0xFF0782c8),
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,

                            )
                    )
                }
                Button(
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF0782c8)),
                    onClick = confirmAction
                ) {
                    Text(stringResource(R.string.confirm).uppercase(), color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun TEIContent(item: ListCardUiModel) {
    val itemList = item.additionalInfo.filter { !it.isConstantItem }
    val constantItemList = item.additionalInfo.filter { it.isConstantItem }

    return Column {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                item.title,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Normal,
            )
            item.subTitle?.let {
                Text(it)
            }
        }

        ConfirmationKeyValueList(constantItemList)
        ConfirmationKeyValueList(itemList)
    }
}


