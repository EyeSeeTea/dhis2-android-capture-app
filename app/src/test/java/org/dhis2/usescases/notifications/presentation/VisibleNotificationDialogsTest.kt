package org.dhis2.usescases.notifications.presentation
// EyeSeeTea customization - Notifications system

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the dialog stacking defect found on 2026-09-17: a notification stays pending until it
 * is accepted, so every resume asks to render it again, and without this tracker the same screen
 * ended up with several dialogs for the same notification on top of each other.
 */
class VisibleNotificationDialogsTest {
    @Test
    fun `a notification that has not been shown yet can be shown`() {
        val dialogs = givenNoVisibleDialogs()

        assertFalse(dialogs.isVisible(NOTIFICATION_ID))
    }

    @Test
    fun `a notification whose dialog is on screen is not shown again`() {
        val dialogs = givenAVisibleDialogFor(NOTIFICATION_ID)

        assertTrue(dialogs.isVisible(NOTIFICATION_ID))
    }

    @Test
    fun `a notification dismissed without accepting can be shown again`() {
        val dialogs = givenAVisibleDialogFor(NOTIFICATION_ID)

        dialogs.onDismissed(NOTIFICATION_ID)

        assertFalse(dialogs.isVisible(NOTIFICATION_ID))
    }

    @Test
    fun `showing one notification does not block a different one`() {
        val dialogs = givenAVisibleDialogFor(NOTIFICATION_ID)

        assertFalse(dialogs.isVisible(ANOTHER_NOTIFICATION_ID))
    }

    @Test
    fun `dismissing one notification does not release a different one`() {
        val dialogs = givenAVisibleDialogFor(NOTIFICATION_ID)
        dialogs.onShown(ANOTHER_NOTIFICATION_ID)

        dialogs.onDismissed(NOTIFICATION_ID)

        assertTrue(dialogs.isVisible(ANOTHER_NOTIFICATION_ID))
    }

    @Test
    fun `resuming the same screen several times keeps a single dialog`() {
        val dialogs = givenNoVisibleDialogs()
        var dialogsBuilt = 0

        repeat(3) {
            if (!dialogs.isVisible(NOTIFICATION_ID)) {
                dialogsBuilt++
                dialogs.onShown(NOTIFICATION_ID)
            }
        }

        assertTrue(dialogsBuilt == 1)
    }

    private fun givenNoVisibleDialogs() = VisibleNotificationDialogs()

    private fun givenAVisibleDialogFor(notificationId: String) =
        VisibleNotificationDialogs().apply { onShown(notificationId) }

    private companion object {
        const val NOTIFICATION_ID = "LvVqMcYFHUd"
        const val ANOTHER_NOTIFICATION_ID = "LvVqMcYFHU3"
    }
}
