package org.dhis2.mobile.commons.error

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.commons.resources.D2ErrorMessageProvider
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

// EyeSeeTea customization - Disabled account login
class DisabledAccountErrorMappingTest {
    @Test
    fun `user account disabled maps to permission denied with disabled account message`() =
        runTest {
            val expectedMessage =
                "Your user account have been disabled. If this is an error, contact your administrator."
            val error =
                D2Error
                    .builder()
                    .errorCode(D2ErrorCode.USER_ACCOUNT_DISABLED)
                    .errorDescription("Account disabled")
                    .build()
            val messageProvider: D2ErrorMessageProvider = mock()
            whenever(messageProvider.getErrorMessage(error, true)).thenReturn(expectedMessage)
            val mapper =
                DomainErrorMapper(
                    d2ErrorMessageProvider = messageProvider,
                    networkStatusProvider = ConnectedNetworkStatusProvider,
                )

            val result = mapper.mapToDomainError(error)

            assertIs<DomainError.PermissionDeniedError>(result)
            assertEquals(
                expectedMessage,
                result.message,
            )
        }

    private object ConnectedNetworkStatusProvider : NetworkStatusProvider {
        override val connectionStatus: Flow<Boolean> = flowOf(true)
    }
}
