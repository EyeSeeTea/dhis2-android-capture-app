package org.dhis2.usescases.main.domain
// Flavor scaffolding, not a customization: upstream made this a per-flavor file in 3.4.x, so
// every flavor source set needs its own copy. This is the in-app APK download, matching what
// develop-widp did through versionRepository.download(). The eyeseetea flavor opens a URL
// instead; WIDP is not distributed through the Play Store.

import android.content.Context
import kotlinx.coroutines.suspendCancellableCoroutine
import org.dhis2.data.service.VersionRepository
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.main.domain.model.DownloadMethod
import kotlin.coroutines.resume

class DownloadNewVersion(
    private val versionRepository: VersionRepository,
) : UseCase<Context, DownloadMethod> {
    override suspend fun invoke(input: Context): Result<DownloadMethod> =
        try {
            suspendCancellableCoroutine { continuation ->
                versionRepository.download(
                    context = input,
                    onDownloadCompleted = {
                        continuation.resume(Result.success(DownloadMethod.File(it)))
                    },
                    onDownloading = {
                        // no-op
                    },
                )
                continuation.invokeOnCancellation {
                    // If needed perform action on cancellation
                }
            }
        } catch (e: DomainError) {
            Result.failure(e)
        }
}
