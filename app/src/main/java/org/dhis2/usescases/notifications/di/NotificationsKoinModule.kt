package org.dhis2.usescases.notifications.di
// EyeSeeTea customization - Notifications system

import org.dhis2.commons.prefs.BasicPreferenceProviderImpl
import org.dhis2.data.notifications.NotificationD2Repository
import org.dhis2.data.notifications.NotificationsApi
import org.dhis2.data.notifications.UserD2Repository
import org.dhis2.data.notifications.UserGroupsApi
import org.dhis2.usescases.notifications.domain.GetNotifications
import org.dhis2.usescases.notifications.domain.MarkNotificationAsRead
import org.dhis2.usescases.notifications.domain.NotificationRepository
import org.dhis2.usescases.notifications.domain.SyncNotifications
import org.dhis2.usescases.notifications.domain.UserRepository
import org.dhis2.usescases.notifications.presentation.NotificationsPresenter
import org.hisp.dhis.android.core.D2
import org.koin.dsl.module

/**
 * Koin graph for the notifications capability. Replaced the Dagger `NotificationsModule` in 3.4.1:
 * upstream migrated `MainActivity` to Koin and dropped its `inject()` call, which was the only
 * thing populating `ActivityGlobalAbstract.notificationsPresenter`. See
 * `eyeseetea-docs/upgrade/widp/upgrade-3.4.1-notes.md`.
 *
 * `BasicPreferenceProvider` is built inline instead of being published to the graph: it is an Oslo
 * type we do not own, and declaring it here would collide the day upstream registers it in Koin.
 */
val notificationsModule =
    module {
        single<NotificationRepository> {
            val d2 = get<D2>()
            NotificationD2Repository(
                d2,
                BasicPreferenceProviderImpl(get()),
                NotificationsApi(d2.httpServiceClient()),
                UserGroupsApi(d2.httpServiceClient()),
            )
        }

        single<UserRepository> { UserD2Repository(get()) }

        factory { GetNotifications(get()) }
        factory { MarkNotificationAsRead(get(), get()) }
        factory { SyncNotifications(get()) }

        single { NotificationsPresenter(get(), get(), get()) }
    }
