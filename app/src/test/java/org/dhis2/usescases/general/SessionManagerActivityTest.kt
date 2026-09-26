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
        var registeredCallback: ((LogOutReason) -> Unit)? = null

        registerSessionTerminationNavigation(
            activityClass = LoginActivity::class.java,
            registerCallback = { callback -> registeredCallback = callback },
            navigateToLogin = {},
        )

        assertThat(registeredCallback).isNull()
    }

    @Test
    fun `authenticated activity navigates to login with disabled account reason`() {
        var registeredCallback: ((LogOutReason) -> Unit)? = null
        var navigationReason: LogOutReason? = null

        registerSessionTerminationNavigation(
            activityClass = MainActivity::class.java,
            registerCallback = { callback -> registeredCallback = callback },
            navigateToLogin = { reason -> navigationReason = reason },
        )
        registeredCallback?.invoke(LogOutReason.DISABLED_ACCOUNT)

        assertThat(registeredCallback).isNotNull()
        assertThat(navigationReason).isEqualTo(LogOutReason.DISABLED_ACCOUNT)
    }
}
