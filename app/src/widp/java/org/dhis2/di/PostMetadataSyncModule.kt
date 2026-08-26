// EyeSeeTea customization - Notifications system
// WIDP behavior: the notification list must be refreshed whenever metadata is synced, not only
// at login. Baseline moved metadata sync into the KMP `:sync` module, which cannot see this
// module, so the work is registered through the `PostMetadataSyncAction` extension point instead.
// Flavor-specific file: other flavors register no actions.
package org.dhis2.di

import org.dhis2.commons.prefs.BasicPreferenceProviderImpl
import org.dhis2.data.notifications.NotificationD2Repository
import org.dhis2.data.notifications.NotificationsApi
import org.dhis2.data.notifications.UserGroupsApi
import org.dhis2.mobile.commons.domain.PostMetadataSyncAction
import org.hisp.dhis.android.core.D2
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val postMetadataSyncModule =
    module {
        factory<List<PostMetadataSyncAction>> {
            listOf(
                PostMetadataSyncAction {
                    // Built inside the action, not in the factory: the repository is only
                    // needed when a metadata sync actually completes, and building it here
                    // avoids the lambda holding on to D2 and the Context until then.
                    val d2 = get<D2>()
                    val repository =
                        NotificationD2Repository(
                            d2,
                            BasicPreferenceProviderImpl(androidContext()),
                            NotificationsApi(d2.httpServiceClient()),
                            UserGroupsApi(d2.httpServiceClient()),
                        )

                    // collect(), not first()/firstOrNull(): those cancel the flow with an
                    // AbortFlowException that sync()'s broad catch logs as a spurious error.
                    runCatching { repository.sync().collect { } }
                },
            )
        }
    }
