package org.dhis2.mobile.sync.data

import io.reactivex.Completable
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.dhis2.mobile.commons.providers.LAST_RETENTION_PURGE
import org.dhis2.mobile.commons.providers.LAST_RETENTION_PURGE_STATUS
import org.dhis2.mobile.commons.providers.PreferenceProvider
import org.dhis2.mobile.commons.reporting.AnalyticActions
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.call.BaseD2Progress
import org.hisp.dhis.android.core.fileresource.FileResourceDomainType
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.settings.GeneralSettings
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AndroidSyncRepositoryTest {
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val preferences = FakePreferenceProvider()
    private val analyticsHelper: AnalyticActions = mock()
    private val domainErrorMapper: DomainErrorMapper = mock()

    private val testDispatcher = StandardTestDispatcher()

    val repository =
        AndroidSyncRepository(
            d2 = d2,
            preferences = preferences,
            analyticsHelper = analyticsHelper,
            domainErrorMapper = domainErrorMapper,
            dispatcher =
                Dispatcher(
                    testDispatcher,
                    testDispatcher,
                    testDispatcher,
                ),
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Should configure secondary tracker if configuration exists`() =
        runTest {
            whenever(
                d2.settingModule().generalSetting().blockingGet(),
            ) doReturn
                GeneralSettings
                    .builder()
                    .encryptDB(false)
                    .matomoID(11111)
                    .matomoURL("MatomoURL")
                    .build()
            whenever(
                d2.mapsModule().mapLayersDownloader().downloadMetadata(),
            ) doReturn Completable.complete()

            whenever(
                d2
                    .fileResourceModule()
                    .fileResourceDownloader()
                    .byDomainType()
                    .eq(FileResourceDomainType.ICON)
                    .download(),
            ) doReturn Observable.just(BaseD2Progress.empty(1))

            repository.updateProjectAnalytics()

            verify(analyticsHelper, times(1)).updateMatomoSecondaryTracker(any(), any())
        }

    @Test
    fun `Should not configure secondary tracker if matomo settings is missing`() =
        runTest {
            whenever(
                d2.settingModule().generalSetting().blockingGet(),
            ) doReturn
                GeneralSettings
                    .builder()
                    .encryptDB(false)
                    .build()
            whenever(
                d2.mapsModule().mapLayersDownloader().downloadMetadata(),
            ) doReturn Completable.complete()
            whenever(
                d2
                    .fileResourceModule()
                    .fileResourceDownloader()
                    .byDomainType()
                    .eq(FileResourceDomainType.ICON)
                    .download(),
            ) doReturn Observable.just(BaseD2Progress.empty(1))

            repository.updateProjectAnalytics()

            verifyNoMoreInteractions(analyticsHelper)
        }

    @Test
    fun `Should not configure secondary tracker if no configuration exists`() =
        runTest {
            whenever(
                d2.settingModule().generalSetting().blockingGet(),
            ) doReturn null
            whenever(
                d2.mapsModule().mapLayersDownloader().downloadMetadata(),
            ) doReturn Completable.complete()
            whenever(
                d2.mapsModule().mapLayersDownloader().downloadMetadata(),
            ) doReturn Completable.complete()
            whenever(
                d2
                    .fileResourceModule()
                    .fileResourceDownloader()
                    .byDomainType()
                    .eq(FileResourceDomainType.ICON)
                    .download(),
            ) doReturn Observable.just(BaseD2Progress.empty(1))
            repository.updateProjectAnalytics()

            verify(analyticsHelper, times(0)).updateMatomoSecondaryTracker(any(), any())
        }

    @Test
    fun `Should clear secondary tracker`() =
        runTest {
            whenever(
                d2.settingModule().generalSetting().blockingGet(),
            ) doReturn null
            whenever(
                d2.mapsModule().mapLayersDownloader().downloadMetadata(),
            ) doReturn Completable.complete()
            whenever(
                d2
                    .fileResourceModule()
                    .fileResourceDownloader()
                    .byDomainType()
                    .eq(FileResourceDomainType.ICON)
                    .download(),
            ) doReturn Observable.just(BaseD2Progress.empty(1))
            repository.updateProjectAnalytics()

            verify(analyticsHelper, times(0)).updateMatomoSecondaryTracker(any(), any())
            verify(analyticsHelper).clearMatomoSecondaryTracker()
        }

    // EyeSeeTea customization - Synced Data Retention Purge
    @Test
    fun `Should persist last purge timestamp when purge succeeds`() =
        runTest {
            repository.purgeRetention()

            assertNotNull(preferences.getString(LAST_RETENTION_PURGE))
        }

    @Test
    fun `Should persist last purge timestamp when purge fails`() =
        runTest {
            givenRetentionPurgeFails(errorDescription = "purge failed")

            repository.purgeRetention()

            assertNotNull(preferences.getString(LAST_RETENTION_PURGE))
        }

    @Test
    fun `Should persist a successful status when purge succeeds`() =
        runTest {
            repository.purgeRetention()

            assertTrue(preferences.getBoolean(LAST_RETENTION_PURGE_STATUS, false))
        }

    @Test
    fun `Should persist a failed status when purge fails`() =
        runTest {
            givenRetentionPurgeFails(errorDescription = "purge failed")

            repository.purgeRetention()

            assertFalse(preferences.getBoolean(LAST_RETENTION_PURGE_STATUS, true))
        }

    private suspend fun givenRetentionPurgeFails(errorDescription: String) {
        val d2Error =
            D2Error
                .builder()
                .errorCode(D2ErrorCode.UNEXPECTED)
                .errorDescription(errorDescription)
                .build()
        whenever(d2.retentionModule().purge()).doAnswer { throw d2Error }
        whenever(domainErrorMapper.mapToDomainError(any())) doReturn
            DomainError.UnexpectedError(errorDescription)
    }
}

// EyeSeeTea customization - Synced Data Retention Purge
private class FakePreferenceProvider(
    delegate: PreferenceProvider = mock(),
) : PreferenceProvider by delegate {
    private val values = mutableMapOf<String, Any?>()

    override fun setValue(
        key: String,
        value: Any?,
    ) {
        values[key] = value
    }

    override fun getString(
        key: String,
        default: String?,
    ): String? = values[key] as? String ?: default

    override fun getBoolean(
        key: String,
        default: Boolean,
    ): Boolean = values[key] as? Boolean ?: default
}
