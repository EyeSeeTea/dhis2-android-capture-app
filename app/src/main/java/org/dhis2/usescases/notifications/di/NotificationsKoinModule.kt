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
import org.hisp.dhis.android.core.D2
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * D2 comes from the Koin graph (published by `serverModule`) rather than from the `D2Manager`
 * static: that definition instantiates D2 if it is not up yet, so resolving this graph before
 * login cannot throw. Everything here is resolved lazily, at first injection.
 */
val notificationsModule =
    module {
        single<BasicPreferenceProvider> { BasicPreferenceProviderImpl(androidContext()) }

        single<NotificationRepository> {
            val d2 = get<D2>()
            NotificationD2Repository(
                d2,
                get(),
                NotificationsApi(d2.httpServiceClient()),
                UserGroupsApi(d2.httpServiceClient()),
            )
        }

        single<UserRepository> { UserD2Repository(get<D2>()) }

        factory { GetNotifications(get()) }

        factory { MarkNotificationAsRead(get(), get()) }

        single { NotificationsPresenter(get(), get()) }
    }
