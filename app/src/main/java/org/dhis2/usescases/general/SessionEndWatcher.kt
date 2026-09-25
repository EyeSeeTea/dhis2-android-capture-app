package org.dhis2.usescases.general
// EyeSeeTea customization - 2FA support

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.qrScanner.ScanActivity
import org.dhis2.usescases.splash.SplashActivity
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
 * Two checks, because the event is a PublishSubject and is only delivered while someone listens.
 * Each resumed screen listens until it pauses, and on resume it also checks that there still is a
 * logged-in user, which catches a session that ended while no screen was listening — during a
 * background sync or a screen transition.
 *
 * Not for the screens in front of a session: the splash, the login screen, and the QR scanner the
 * login screen opens.
 *
 * Kept out of the base activity so it can be unit tested; the base activity has no test harness
 * here. RxJava only because that is what the SDK exposes.
 */
class SessionEndWatcher(
    private val isLoggedIn: () -> Boolean,
    private val sessionEnded: Observable<Unit>,
    private val observeOn: Scheduler,
) {
    private var subscription: Disposable? = null

    /**
     * Starts listening for [screen]. Returns false if the session had already ended, after
     * calling [onEnded]; [onEnded] is called at most once.
     */
    fun start(
        screen: Class<*>,
        onEnded: () -> Unit,
    ): Boolean {
        if (!watches(screen)) return true
        if (!isLoggedIn()) {
            onEnded()
            return false
        }
        subscription =
            sessionEnded.observeOn(observeOn).subscribe(
                {
                    stop()
                    onEnded()
                },
                { Timber.e(it) },
            )
        return true
    }

    fun stop() {
        subscription?.dispose()
        subscription = null
    }

    companion object {
        private val screensBeforeASession: Set<Class<*>> =
            setOf(SplashActivity::class.java, LoginActivity::class.java, ScanActivity::class.java)

        fun watches(screen: Class<*>): Boolean = screensBeforeASession.none { it.isAssignableFrom(screen) }
    }
}
