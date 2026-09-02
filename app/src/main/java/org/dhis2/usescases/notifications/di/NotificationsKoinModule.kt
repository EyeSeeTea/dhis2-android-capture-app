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
import org.dhis2.usescases.notifications.domain.UserRepository
import org.dhis2.usescases.notifications.presentation.NotificationsPresenter
import org.hisp.dhis.android.core.D2
import org.koin.dsl.module

/**
 * Replaces the Dagger `NotificationsModule` / `NotificationsComponent` pair.
 *
 * Upstream 3.4 migrated `MainActivity` to Koin and stopped running the Dagger `inject()`
 * that populated the inherited `notificationsPresenter`, which left it null and crashed on
 * entering the main screen. The graph therefore lives in Koin.
 *
 * `D2` is taken from the Koin graph (published by `serverModule`) rather than the
 * `D2Manager` static, and `BasicPreferenceProvider` is built inline so that an Oslo type is
 * not published to the shared graph.
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
        single { NotificationsPresenter(get(), get()) }
    }
