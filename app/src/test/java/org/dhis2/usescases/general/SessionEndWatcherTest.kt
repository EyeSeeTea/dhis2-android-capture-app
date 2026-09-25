package org.dhis2.usescases.general
// EyeSeeTea customization - 2FA support

import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.qrScanner.ScanActivity
import org.dhis2.usescases.splash.SplashActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the defect found on the device on 2026-09-25: with a 2FA account, after an app restart
 * the server rejected the session and the SDK announced it, but the app kept the user on the Home
 * with every request failing, and logging out crashed.
 */
class SessionEndWatcherTest {
    private val sessionEnded = PublishSubject.create<Unit>()
    private var returnsToLogin = 0

    @Test
    fun `a screen keeps going while the session is alive`() {
        val keepsGoing = givenAWatcher(loggedIn = true).start(MainActivity::class.java) { returnsToLogin++ }

        assertTrue(keepsGoing)
        assertEquals(0, returnsToLogin)
    }

    @Test
    fun `a screen returns to login when the session ended while nothing was listening`() {
        val keepsGoing = givenAWatcher(loggedIn = false).start(MainActivity::class.java) { returnsToLogin++ }

        assertFalse(keepsGoing)
        assertEquals(1, returnsToLogin)
    }

    @Test
    fun `the end of the session announced by the SDK returns to login`() {
        givenAWatcher(loggedIn = true).start(MainActivity::class.java) { returnsToLogin++ }

        sessionEnded.onNext(Unit)

        assertEquals(1, returnsToLogin)
    }

    @Test
    fun `several announcements return to login only once`() {
        // A rejected session fails every request in flight, and each one announces it.
        givenAWatcher(loggedIn = true).start(MainActivity::class.java) { returnsToLogin++ }

        sessionEnded.onNext(Unit)
        sessionEnded.onNext(Unit)

        assertEquals(1, returnsToLogin)
    }

    @Test
    fun `a paused screen ignores the announcement`() {
        val watcher = givenAWatcher(loggedIn = true)
        watcher.start(MainActivity::class.java) { returnsToLogin++ }

        watcher.stop()
        sessionEnded.onNext(Unit)

        assertEquals(0, returnsToLogin)
    }

    @Test
    fun `the screens in front of a session never return to login`() {
        listOf(SplashActivity::class.java, LoginActivity::class.java, ScanActivity::class.java).forEach {
            val keepsGoing = givenAWatcher(loggedIn = false).start(it) { returnsToLogin++ }
            sessionEnded.onNext(Unit)

            assertTrue(keepsGoing)
        }
        assertEquals(0, returnsToLogin)
    }

    private fun givenAWatcher(loggedIn: Boolean) =
        SessionEndWatcher(
            isLoggedIn = { loggedIn },
            sessionEnded = sessionEnded,
            observeOn = Schedulers.trampoline(),
        )
}
