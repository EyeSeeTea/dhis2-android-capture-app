package org.dhis2.di
// EyeSeeTea customization - Notifications system
// Base behavior: a metadata sync downloads metadata only.
// widp behavior: it also refreshes the notification list from the DHIS2 datastore.

import org.dhis2.mobile.commons.domain.PostMetadataSyncAction
import org.dhis2.usescases.notifications.domain.NotificationRepository
import org.dhis2.usescases.notifications.presentation.NotificationsPresenter
import org.koin.dsl.module

/**
 * Downloads WIDP notifications after every successful metadata sync.
 *
 * Until 3.4.0 this ran from `SyncPresenterImpl.syncMetadata().doOnComplete {}`. Upstream moved
 * metadata sync into the `:sync` module, which cannot depend on `:app`, so that hook is gone.
 * `PostMetadataSyncAction` is the replacement the baseline promoted for exactly this case; see
 * `eyeseetea-docs/customization-techniques.md` T2.
 *
 * Registered as a single `List<PostMetadataSyncAction>`, not as individual definitions: two bare
 * `factory<PostMetadataSyncAction>` bindings would overwrite each other without qualifiers.
 */
val postMetadataSyncModule =
    module {
        factory<List<PostMetadataSyncAction>> {
            val notificationRepository = get<NotificationRepository>()
            val notificationsPresenter = get<NotificationsPresenter>()
            listOf(
                PostMetadataSyncAction {
                    runCatching {
                        // collect {}, never first(): the terminal first* operators cancel the flow
                        // with AbortFlowException, which the repository's broad catch swallows and
                        // logs as a failure after the work has already succeeded.
                        notificationRepository.sync().collect { }
                        notificationsPresenter.markShowNotificationsAsPending()
                    }
                },
            )
        }
    }
