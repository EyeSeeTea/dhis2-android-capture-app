package org.dhis2.data.server
// EyeSeeTea customization - 2FA support

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.server.OpenIdSession.LogOutReason
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.user.AccountDeletionReason
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Covers the automatic logout. When the server rejects the session — typically a 2FA account after
 * an app restart — the SDK removes the stored credentials and announces it on
 * `accountManager().logOutObservable()`. The subscription was removed once as "dead custom code",
 * and the app then stayed on the Home with every request failing; logging out crashed because the
 * credentials were already gone.
 */
class OpenIdSessionTest {
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val sdkLogOut = PublishSubject.create<Unit>()
    private val reasons = mutableListOf<LogOutReason>()

    @Before
    fun setUp() {
        whenever(d2.userModule().openIdHandler().logOutObservable()) doReturn Observable.never()
        whenever(d2.userModule().accountManager().accountDeletionObservable()) doReturn
            Observable.never<AccountDeletionReason>()
        whenever(d2.userModule().accountManager().logOutObservable()) doReturn sdkLogOut
    }

    @Test
    fun `the SDK automatic logout takes the user back to login as unauthorized`() {
        givenAResumedScreen()

        sdkLogOut.onNext(Unit)

        assertEquals(listOf(LogOutReason.UNAUTHORIZED), reasons)
    }

    @Test
    fun `a paused screen ignores the SDK automatic logout`() {
        val session = givenAResumedScreen()

        session.onDestroy()
        sdkLogOut.onNext(Unit)

        assertEquals(emptyList<LogOutReason>(), reasons)
    }

    private fun givenAResumedScreen(): OpenIdSession {
        val owner: LifecycleOwner =
            mock { on { lifecycle } doReturn mock<Lifecycle>() }
        val session = OpenIdSession(d2, TrampolineSchedulerProvider())
        session.setSessionCallback(owner) { reasons += it }
        session.onCreate()
        return session
    }
}
