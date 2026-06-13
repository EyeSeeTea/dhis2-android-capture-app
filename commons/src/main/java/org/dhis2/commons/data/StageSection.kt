package org.dhis2.commons.data

// EyeSeeTea customization - bounded TEI event list
// Number of additional events revealed by each "show more" action. Bounding the
// reveal (instead of expanding to the full list) keeps the event list memory
// proportional to what the user requested: the dashboard RecyclerView lives in a
// NestedScrollView, so every submitted item is inflated eagerly (no recycling).
const val EVENTS_PAGE_SIZE = 25

// EyeSeeTea customization - bounded TEI event list
// Visible window for a stage/timeline: never fewer than the initial cap, never
// more than requested via paging, never more than the list holds.
fun visibleEventCount(
    totalEvents: Int,
    initialCap: Int,
    revealedEventCount: Int,
): Int = minOf(totalEvents, maxOf(initialCap, revealedEventCount))

data class StageSection(
    val stageUid: String,
    val showOptions: Boolean,
    // EyeSeeTea customization - bounded TEI event list
    // 0 = initial capped view; > 0 = number of events the user revealed via paging.
    val revealedEventCount: Int = 0,
)
