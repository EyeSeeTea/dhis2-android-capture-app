package org.dhis2.usescases.general
// EyeSeeTea customization - 2FA support

import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
 *
 * No runTest on purpose: a known upstream test leaks an exception that fails whichever runTest
 * test runs next, and these tests do not need virtual time.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SessionEndWatcherTest {
    private val sessionEnded = MutableSharedFlow<Unit>(extraBufferCapacity = 2)
    private val screenScope = CoroutineScope(UnconfinedTestDispatcher())
    private var returnsToLogin = 0

    @Test
    fun `a screen keeps going while the session is alive`() {
        val keepsGoing = givenAWatcher(loggedIn = true).startOn(MainActivity::class.java)

        assertTrue(keepsGoing)
        assertEquals(0, returnsToLogin)
    }

    @Test
    fun `a screen returns to login when the session ended while nothing was listening`() {
        val keepsGoing = givenAWatcher(loggedIn = false).startOn(MainActivity::class.java)

        assertFalse(keepsGoing)
        assertEquals(1, returnsToLogin)
    }

    @Test
    fun `the end of the session announced by the SDK returns to login`() {
        givenAWatcher(loggedIn = true).startOn(MainActivity::class.java)

        sessionEnded.tryEmit(Unit)

        assertEquals(1, returnsToLogin)
    }

    @Test
    fun `several announcements return to login only once`() {
        // A rejected session fails every request in flight, and each one announces it.
        givenAWatcher(loggedIn = true).startOn(MainActivity::class.java)

        sessionEnded.tryEmit(Unit)
        sessionEnded.tryEmit(Unit)

        assertEquals(1, returnsToLogin)
    }

    @Test
    fun `a paused screen ignores the announcement`() {
        val watcher = givenAWatcher(loggedIn = true)
        watcher.startOn(MainActivity::class.java)

        watcher.stop()
        sessionEnded.tryEmit(Unit)

        assertEquals(0, returnsToLogin)
    }

    @Test
    fun `the screens in front of a session never return to login`() {
        listOf(SplashActivity::class.java, LoginActivity::class.java, ScanActivity::class.java).forEach {
            val keepsGoing = givenAWatcher(loggedIn = false).startOn(it)
            sessionEnded.tryEmit(Unit)

            assertTrue(keepsGoing)
        }
        assertEquals(0, returnsToLogin)
    }

    @Test
    fun `the SDK event reaches the flow, and stops being observed when nobody listens`() {
        val sdkEvent = PublishSubject.create<Unit>()
        var received = 0
        val listening = screenScope.launch { sdkEvent.asFlow().first(); received++ }

        sdkEvent.onNext(Unit)

        assertEquals(1, received)
        assertTrue(listening.isCompleted)
        assertFalse(sdkEvent.hasObservers())
    }

    private fun givenAWatcher(loggedIn: Boolean) =
        SessionEndWatcher(isLoggedIn = { loggedIn }, sessionEnded = sessionEnded)

    private fun SessionEndWatcher.startOn(screen: Class<*>) =
        start(screen, screenScope) { returnsToLogin++ }
}
