package org.dhis2.usescases.general

import com.google.common.truth.Truth.assertThat
import org.dhis2.data.server.OpenIdSession.LogOutReason
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.main.MainActivity
import org.junit.Test

// EyeSeeTea customization - Disabled account login
class SessionManagerActivityTest {
    @Test
    fun `login does not register session termination navigation`() {
        val callback = sessionTerminationNavigationCallback(LoginActivity::class.java) {}

        assertThat(callback).isNull()
    }

    @Test
    fun `authenticated activity navigates to login with disabled account reason`() {
        var navigationReason: LogOutReason? = null
        val callback = sessionTerminationNavigationCallback(MainActivity::class.java) { reason ->
            navigationReason = reason
        }

        callback?.invoke(LogOutReason.DISABLED_ACCOUNT)

        assertThat(callback).isNotNull()
        assertThat(navigationReason).isEqualTo(LogOutReason.DISABLED_ACCOUNT)
    }
}
