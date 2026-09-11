package org.dhis2.usescases.notifications.di
// EyeSeeTea customization - Notifications system
// Base behavior: none — the notifications graph does not exist upstream.
// This module replaces the Dagger NotificationsModule. Upstream migrated MainActivity to Koin
// in 3.4.x and stopped running the inject() that used to populate the presenter inherited by
// ActivityGlobalAbstract, so the graph has to be published where that base class can reach it.

import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.commons.prefs.BasicPreferenceProviderImpl
import org.dhis2.data.notifications.NotificationD2Repository
import org.dhis2.data.notifications.NotificationsApi
import org.dhis2.data.notifications.UserD2Repository
import org.dhis2.data.notifications.UserGroupsApi
import org.dhis2.usescases.notifications.domain.GetNotifications
import org.dhis2.usescases.notifications.domain.MarkNotificationAsRead
import org.dhis2.usescases.notifications.domain.NotificationRepository
import org.dhis2.usescases.notifications.domain.UserRepository
import org.dhis2.usescases.notifications.presentation.NotificationsPresenter
import org.hisp.dhis.android.core.D2Manager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Every definition here resolves D2 lazily, at first injection rather than at startup: the
 * repository needs an initialised D2, and Koin is started before login.
 */
val notificationsModule =
    module {
        single<BasicPreferenceProvider> { BasicPreferenceProviderImpl(androidContext()) }

        single<NotificationRepository> {
            val d2 = D2Manager.getD2()
            NotificationD2Repository(
                d2,
                get(),
                NotificationsApi(d2.httpServiceClient()),
                UserGroupsApi(d2.httpServiceClient()),
            )
        }

        single<UserRepository> { UserD2Repository(D2Manager.getD2()) }

        single { GetNotifications(get()) }

        single { MarkNotificationAsRead(get(), get()) }

        single { NotificationsPresenter(get(), get()) }
    }
