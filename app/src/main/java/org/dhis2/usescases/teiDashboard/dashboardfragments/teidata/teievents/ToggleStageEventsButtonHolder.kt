package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.processors.FlowableProcessor
import org.dhis2.R
import org.dhis2.commons.data.EVENTS_PAGE_SIZE
import org.dhis2.commons.data.EventModel
import org.dhis2.commons.data.StageSection
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing

class ToggleStageEventsButtonHolder(
    val composeView: ComposeView,
    private val stageSelector: FlowableProcessor<StageSection>,
) : RecyclerView.ViewHolder(composeView) {
    fun bind(eventModel: EventModel) {
        composeView.setContent {
            Button(
                modifier =
                    Modifier.padding(
                        start =
                            if (eventModel.groupedByStage == true) {
                                Spacing.Spacing48
                            } else {
                                Spacing.Spacing0
                            },
                    ),
                style = ButtonStyle.TEXT,
                text = toggleText(eventModel),
            ) {
                stageSelector.onNext(
                    StageSection(
                        stageUid = eventModel.stage?.uid() ?: "",
                        showOptions = false,
                        // EyeSeeTea customization - bounded TEI event list
                        // Reveal one more page instead of the full list; 0 collapses
                        // back to the initial capped view.
                        revealedEventCount =
                            if (eventModel.showAllEvents) {
                                0
                            } else {
                                eventModel.maxEventsToShow + EVENTS_PAGE_SIZE
                            },
                    ),
                )
            }
        }
    }

    // EyeSeeTea customization - bounded TEI event list
    private fun toggleText(eventModel: EventModel): String {
        val remaining = eventModel.eventCount - eventModel.maxEventsToShow
        return when {
            eventModel.showAllEvents ->
                composeView.context.getString(R.string.show_less_events)

            remaining > EVENTS_PAGE_SIZE ->
                composeView.context.getString(
                    R.string.show_more_events_paged,
                    EVENTS_PAGE_SIZE.toString(),
                    remaining.toString(),
                )

            else ->
                composeView.context.getString(
                    R.string.show_more_events,
                    remaining.toString(),
                )
        }
    }
}
