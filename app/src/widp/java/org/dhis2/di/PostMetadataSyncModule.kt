package org.dhis2.di
// EyeSeeTea customization - Notifications system

import org.dhis2.mobile.commons.domain.PostMetadataSyncAction
import org.dhis2.usescases.notifications.domain.NotificationRepository
import org.dhis2.usescases.notifications.presentation.NotificationsPresenter
import org.koin.dsl.module

/**
 * Downloads the WIDP notifications after every successful metadata sync.
 *
 * Until 3.4.0 this ran inside `SyncPresenterImpl.syncMetadata().doOnComplete {}`. Upstream
 * moved the sync into the `:sync` module, which cannot depend on `:app`, so the baseline
 * provides `PostMetadataSyncAction` as the replacement extension point. Registering here keeps
 * the whole trigger inside the widp flavor source set — no Oslo file is touched — and restores
 * the behaviour the spec requires: the fetch happens during metadata sync, independent of any
 * Activity or ViewModel, so it also covers background periodic syncs with the app closed.
 *
 * A failing action is logged and swallowed by `SyncMetadata`, so a notifications outage can
 * never break syncing.
 */
val postMetadataSyncModule =
    module {
        factory<List<PostMetadataSyncAction>> {
            val notificationRepository = get<NotificationRepository>()
            val notificationsPresenter = get<NotificationsPresenter>()
            listOf(
                PostMetadataSyncAction {
                    runCatching {
                        notificationRepository.sync().collect {}
                        notificationsPresenter.markShowNotificationsAsPending()
                    }
                },
            )
        }
    }
