package org.dhis2.usescases.settings.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.SyncDisabled
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import org.dhis2.R
import org.dhis2.bindings.EVERY_12_HOUR
import org.dhis2.bindings.EVERY_24_HOUR
import org.dhis2.bindings.EVERY_30_MIN
import org.dhis2.bindings.EVERY_6_HOUR
import org.dhis2.bindings.EVERY_HOUR
import org.dhis2.commons.Constants
import org.dhis2.usescases.settings.models.RetentionPurgeSettingsViewModel
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItemColor
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownItem
import org.hisp.dhis.mobile.ui.designsystem.component.InputDropDown
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.dhis2.usescases.settings.SettingItem as SettingItemType

// EyeSeeTea customization - Synced Data Retention Purge
@Composable
internal fun RetentionPurgeSettingItem(
    retentionPurgeSettings: RetentionPurgeSettingsViewModel,
    canInitPurge: Boolean,
    isOpened: Boolean,
    onClick: () -> Unit,
    onPurgeNowClick: () -> Unit,
    onRetentionPurgePeriodChanged: (Int) -> Unit,
    context: Context = LocalContext.current,
) {
    val additionalInfoList =
        when {
            retentionPurgeSettings.purgeInProgress -> provideRetentionPurgeInProgressInfo(retentionPurgeSettings.purgePeriod, context)
            retentionPurgeSettings.purgeHasErrors -> provideRetentionPurgeErrorInfo(retentionPurgeSettings.purgePeriod, context)
            else -> provideRetentionPurgeDefaultInfo(retentionPurgeSettings, context)
        }

    SettingItem(
        modifier =
            Modifier.semantics {
                testTag = SettingItemType.RETENTION_PURGE.name
            },
        title = stringResource(id = R.string.settingsRetentionPurge),
        additionalInfoList = additionalInfoList,
        icon = Icons.Outlined.DeleteSweep,
        extraActions = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = spacedBy(Spacing.Spacing8),
            ) {
                var selectedItem by
                    remember {
                        mutableStateOf(
                            DropdownItem(
                                label = syncPeriodLabel(retentionPurgeSettings.purgePeriod, context),
                            ),
                        )
                    }
                val purgePeriods =
                    listOf(
                        stringResource(R.string.thirty_minutes),
                        stringResource(R.string.a_hour),
                        stringResource(R.string.every_6_hours),
                        stringResource(R.string.every_12_hours),
                        stringResource(R.string.a_day),
                        stringResource(R.string.Manual),
                    )
                val dropdownItems = purgePeriods.map { DropdownItem(it) }
                var inputPurgePeriodState =
                    remember {
                        InputShellState.UNFOCUSED
                    }
                InputDropDown(
                    modifier = Modifier.testTag(TEST_TAG_RETENTION_PURGE_PERIOD),
                    title = stringResource(R.string.settings_sync_period_v2),
                    state = inputPurgePeriodState,
                    itemCount = purgePeriods.size,
                    onSearchOption = {},
                    fetchItem = { index ->
                        dropdownItems[index]
                    },
                    selectedItem = selectedItem,
                    onResetButtonClicked = { },
                    onItemSelected = { index, newItem ->
                        selectedItem = newItem
                        inputPurgePeriodState = InputShellState.UNFOCUSED
                        when (index) {
                            0 -> onRetentionPurgePeriodChanged(EVERY_30_MIN)
                            1 -> onRetentionPurgePeriodChanged(EVERY_HOUR)
                            2 -> onRetentionPurgePeriodChanged(EVERY_6_HOUR)
                            3 -> onRetentionPurgePeriodChanged(EVERY_12_HOUR)
                            4 -> onRetentionPurgePeriodChanged(EVERY_24_HOUR)
                            5 -> onRetentionPurgePeriodChanged(Constants.TIME_MANUAL)
                            else -> {
                                // do nothing
                            }
                        }
                    },
                    showSearchBar = false,
                    loadOptions = {},
                    showDeleteButton = false,
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text =
                        stringResource(R.string.PURGE_RETENTION)
                            .lowercase()
                            .capitalize(Locale.current),
                    style = ButtonStyle.TONAL,
                    enabled = canInitPurge,
                    onClick = onPurgeNowClick,
                )
            }
        },
        showExtraActions = isOpened,
        onClick = onClick,
    )
}

@Composable
private fun provideRetentionPurgeDefaultInfo(
    retentionPurgeSettings: RetentionPurgeSettingsViewModel,
    context: Context,
): List<AdditionalInfoItem> =
    buildList {
        add(
            AdditionalInfoItem(
                key = stringResource(R.string.settings_sync_period_v2),
                value = syncPeriodLabel(retentionPurgeSettings.purgePeriod, context),
            ),
        )
        add(
            AdditionalInfoItem(
                key = stringResource(R.string.last_retention_purge),
                value = retentionPurgeSettings.lastPurge,
                color = TextColor.OnSurface,
            ),
        )
    }

@Composable
private fun provideRetentionPurgeErrorInfo(
    purgePeriod: Int,
    context: Context,
) = listOf(
    AdditionalInfoItem(
        key = stringResource(R.string.settings_sync_period_v2),
        value = syncPeriodLabel(purgePeriod, context),
        isConstantItem = true,
    ),
    AdditionalInfoItem(
        value = stringResource(R.string.retention_purge_error_text),
        isConstantItem = true,
        icon = {
            Icon(
                imageVector = Icons.Outlined.SyncDisabled,
                contentDescription = "RETENTION PURGE ERROR",
                tint = AdditionalInfoItemColor.ERROR.color,
            )
        },
        color = AdditionalInfoItemColor.ERROR.color,
    ),
)

@Composable
private fun provideRetentionPurgeInProgressInfo(
    purgePeriod: Int,
    context: Context,
) = listOf(
    AdditionalInfoItem(
        key = stringResource(R.string.settings_sync_period_v2),
        value = syncPeriodLabel(purgePeriod, context),
        isConstantItem = true,
    ),
    AdditionalInfoItem(
        value = stringResource(R.string.purging_retention_data),
        isConstantItem = true,
    ),
)
