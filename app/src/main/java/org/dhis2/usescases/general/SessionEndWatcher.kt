package org.dhis2.usescases.general
// EyeSeeTea customization - 2FA support

import io.reactivex.Observable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.qrScanner.ScanActivity
import org.dhis2.usescases.splash.SplashActivity
import org.hisp.dhis.android.core.D2
import timber.log.Timber

/**
 * Sends the user back to the login screen when the server session ends while the app is in use.
 *
 * When the server rejects the session, the SDK removes the stored credentials and announces it on
 * `accountManager().logOutObservable()`. The typical case is a 2FA account after an app restart:
 * that session lives in a cookie held in memory and cannot be rebuilt from the stored password.
 * Nothing in the app listened to that event, so the user stayed on the Home with every request
 * to the server failing, and logging out crashed because the credentials were already gone.
 *
 * Two checks, because the SDK only delivers the event to whoever is listening at that moment.
 * Each resumed screen listens until it pauses, and on resume it also checks that there still is a
 * logged-in user, which catches a session that ended while no screen was listening — during a
 * background sync or a screen transition.
 *
 * Not for the screens in front of a session: the splash, the login screen, and the QR scanner the
 * login screen opens.
 *
 * Kept out of the base activity so it can be unit tested; the base activity has no test harness
 * here.
 */
class SessionEndWatcher(
    private val isLoggedIn: () -> Boolean,
    private val sessionEnded: Flow<Unit>,
) {
    private var listening: Job? = null

    /**
     * Starts listening for [screen] in [scope], the screen's own lifecycle scope. Returns false if
     * the session had already ended, after calling [onEnded]; [onEnded] is called at most once.
     */
    fun start(
        screen: Class<*>,
        scope: CoroutineScope,
        onEnded: () -> Unit,
    ): Boolean {
        if (!watches(screen)) return true
        if (!isLoggedIn()) {
            onEnded()
            return false
        }
        listening =
            scope.launch {
                try {
                    // first(): a rejected session fails every request in flight, and each one
                    // announces it; the user is taken to login once.
                    sessionEnded.first()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Timber.e(e)
                    return@launch
                }
                listening = null
                onEnded()
            }
        return true
    }

    fun stop() {
        listening?.cancel()
        listening = null
    }

    companion object {
        private val screensBeforeASession: Set<Class<*>> =
            setOf(SplashActivity::class.java, LoginActivity::class.java, ScanActivity::class.java)

        fun watches(screen: Class<*>): Boolean = screensBeforeASession.none { it.isAssignableFrom(screen) }
    }
}

/**
 * The SDK's end-of-session event as a Flow. The SDK exposes it as an RxJava Observable; this is the
 * boundary where it is wrapped, so the rest of this code stays on coroutines.
 */
fun sdkSessionEnded(d2: D2): Flow<Unit> = d2.userModule().accountManager().logOutObservable().asFlow()

internal fun <T : Any> Observable<T>.asFlow(): Flow<T> =
    callbackFlow {
        val subscription = subscribe({ trySend(it) }, { close(it) }, { close() })
        awaitClose { subscription.dispose() }
    }
