package org.dhis2.usescases.notifications.di
// EyeSeeTea customization - Notifications system

import android.content.Context
import android.content.SharedPreferences
import org.dhis2.usescases.notifications.domain.GetNotifications
import org.dhis2.usescases.notifications.domain.MarkNotificationAsRead
import org.dhis2.usescases.notifications.domain.NotificationRepository
import org.dhis2.usescases.notifications.domain.SyncNotifications
import org.dhis2.usescases.notifications.domain.UserRepository
import org.dhis2.usescases.notifications.presentation.NotificationsPresenter
import org.hisp.dhis.android.core.D2
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Guards the wiring of the notifications capability. Koin resolves at runtime, so a missing or
 * misconfigured definition still compiles and only fails once the app is on screen — which is
 * exactly how the 3.4.1 upgrade shipped a null `notificationsPresenter`.
 */
class NotificationsModuleTest {
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    private val context: Context = mock()

    // What the host application is expected to publish for this module to resolve.
    private val hostDependencies =
        module {
            single { d2 }
            single { context }
        }

    @Before
    fun setUp() {
        whenever(context.getSharedPreferences(any(), any())) doReturn mock<SharedPreferences>()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    private fun graph(): Koin = startKoin { modules(hostDependencies, notificationsModule) }.koin

    @Test
    fun `resolves the presenter every activity reads`() {
        assertNotNull(graph().get<NotificationsPresenter>())
    }

    @Test
    fun `resolves every use case and repository the presenter depends on`() {
        val koin = graph()

        assertNotNull(koin.get<NotificationRepository>())
        assertNotNull(koin.get<UserRepository>())
        assertNotNull(koin.get<GetNotifications>())
        assertNotNull(koin.get<MarkNotificationAsRead>())
        assertNotNull(koin.get<SyncNotifications>())
    }

    @Test
    fun `shares a single presenter across activities`() {
        val koin = graph()

        assertSame(koin.get<NotificationsPresenter>(), koin.get<NotificationsPresenter>())
    }
}
