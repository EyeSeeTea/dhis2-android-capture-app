package org.dhis2.commons.data

import org.junit.Assert.assertEquals
import org.junit.Test

// EyeSeeTea customization - bounded TEI event list
class StageSectionTest {
    @Test
    fun `should show only the initial cap when nothing is revealed`() {
        assertEquals(3, visibleEventCount(totalEvents = 1036, initialCap = 3, revealedEventCount = 0))
        assertEquals(5, visibleEventCount(totalEvents = 1036, initialCap = 5, revealedEventCount = 0))
    }

    @Test
    fun `should grow by exactly one page per reveal`() {
        val firstReveal = 3 + EVENTS_PAGE_SIZE
        assertEquals(
            28,
            visibleEventCount(totalEvents = 1036, initialCap = 3, revealedEventCount = firstReveal),
        )
        val secondReveal = firstReveal + EVENTS_PAGE_SIZE
        assertEquals(
            53,
            visibleEventCount(totalEvents = 1036, initialCap = 3, revealedEventCount = secondReveal),
        )
    }

    @Test
    fun `should never exceed the total number of events`() {
        assertEquals(10, visibleEventCount(totalEvents = 10, initialCap = 3, revealedEventCount = 28))
        assertEquals(0, visibleEventCount(totalEvents = 0, initialCap = 3, revealedEventCount = 0))
    }

    @Test
    fun `should never show fewer than the initial cap`() {
        assertEquals(3, visibleEventCount(totalEvents = 1036, initialCap = 3, revealedEventCount = 1))
        assertEquals(2, visibleEventCount(totalEvents = 2, initialCap = 3, revealedEventCount = 0))
    }

    @Test
    fun `should collapse back to the initial cap when reveal is reset`() {
        val expanded = visibleEventCount(totalEvents = 1036, initialCap = 3, revealedEventCount = 53)
        assertEquals(53, expanded)
        assertEquals(3, visibleEventCount(totalEvents = 1036, initialCap = 3, revealedEventCount = 0))
    }

    @Test
    fun `should default to no revealed events`() {
        assertEquals(0, StageSection(stageUid = "stage", showOptions = false).revealedEventCount)
    }
}
