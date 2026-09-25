package org.dhis2.usescases.general
// EyeSeeTea customization - 2FA support

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hisp.dhis.android.core.D2
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Asks the server once per app process whether the session is still valid, as soon as the user
 * reaches a screen of the app.
 *
 * A 2FA session does not survive an app restart, but the app only finds out when something talks
 * to the server. On the device on 2026-09-25 that took minutes: the user was left on the Home,
 * free to enter data, believing they were logged in. A minimal authenticated request right away
 * lets the server reject the session early. There is nothing to handle here: a rejection makes the
 * SDK remove the credentials and announce it, and [SessionEndWatcher] takes the user to login.
 *
 * Offline, the request cannot reach the server, so it is tried again on the next screen instead
 * of being counted as done.
 */
object SessionCheckOnStart {
    private val checked = AtomicBoolean(false)

    fun checkOnce(d2: D2) =
        checkOnce(CoroutineScope(Dispatchers.IO)) {
            d2.httpServiceClient().get<String> { url("me?fields=id") }
        }

    internal fun checkOnce(
        scope: CoroutineScope,
        probe: suspend () -> Unit,
    ) {
        if (!checked.compareAndSet(false, true)) return
        scope.launch {
            try {
                probe()
            } catch (e: IOException) {
                // No connection: nothing was learnt about the session, so ask again later.
                checked.set(false)
            } catch (e: Exception) {
                // Any answer from the server counts. A rejected session has already been handled
                // by the SDK and SessionEndWatcher.
                Timber.d(e, "Session check on start")
            }
        }
    }

    internal fun reset() {
        checked.set(false)
    }
}
