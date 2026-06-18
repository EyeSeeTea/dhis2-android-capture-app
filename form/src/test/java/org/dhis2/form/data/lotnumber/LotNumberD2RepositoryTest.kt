package org.dhis2.form.data.lotnumber

// EyeSeeTea customization - Lot Number Search Field

import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.form.model.lotnumber.LotNumbersResult
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class LotNumberD2RepositoryTest {
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val preferenceProvider: PreferenceProvider = mock()
    private val lotNumbersApi: LotNumbersApi = mock()

    private lateinit var repository: LotNumberD2Repository

    private val eventUid = "event_uid"
    private val orgUnitUid = "org_unit_uid"
    private val orgUnitCode = "OU_CODE"
    private val productCode = "PRODUCT_CODE"

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        repository = LotNumberD2Repository(d2, preferenceProvider, lotNumbersApi, testDispatcher)
        givenAnEvent()
        givenAnOrgUnit()
        givenAnEmptyCache()
    }

    @Test
    fun returnsNoProductSelectedWhenProductDataValueIsNull() =
        runTest(testDispatcher) {
            givenNoProduct()

            val result = repository.getLotNumbers(eventUid)

            assertEquals(LotNumbersResult.NoProductSelected, result)
        }

    @Test
    fun returnsRemoteLotNumbersAndUpdatesCacheWhenNetworkSucceeds() =
        runTest(testDispatcher) {
            val remoteData = mapOf(orgUnitCode to mapOf(productCode to LotNumbersEntry(listOf("LOT1", "LOT2"))))
            givenAProduct(productCode)
            givenRemoteData(remoteData)

            val result = repository.getLotNumbers(eventUid)

            assertEquals(LotNumbersResult.Available(listOf("LOT1", "LOT2")), result)
            verify(preferenceProvider).saveAsJson(Preference.LOT_NUMBERS_CACHE, remoteData)
        }

    @Test
    fun fallsBackToCacheWhenNetworkFails() =
        runTest(testDispatcher) {
            val cachedData = mapOf(orgUnitCode to mapOf(productCode to LotNumbersEntry(listOf("CACHED_LOT"))))
            givenAProduct(productCode)
            givenNetworkError()
            givenCachedData(cachedData)

            val result = repository.getLotNumbers(eventUid)

            assertEquals(LotNumbersResult.Available(listOf("CACHED_LOT")), result)
        }

    @Test
    fun returnsNotFoundWhenNetworkFailsAndCacheIsEmpty() =
        runTest(testDispatcher) {
            givenAProduct(productCode)
            givenNetworkError()

            val result = repository.getLotNumbers(eventUid)

            assertEquals(LotNumbersResult.NotFound, result)
        }

    private fun givenAnEvent() {
        val event: Event = mock()
        whenever(event.organisationUnit()) doReturn orgUnitUid
        whenever(
            d2
                .eventModule()
                .events()
                .uid(eventUid)
                .blockingGet(),
        ) doReturn event
    }

    private fun givenAnOrgUnit() {
        val organisationUnit: OrganisationUnit = mock()
        whenever(organisationUnit.code()) doReturn orgUnitCode
        whenever(
            d2
                .organisationUnitModule()
                .organisationUnits()
                .uid(orgUnitUid)
                .blockingGet(),
        ) doReturn organisationUnit
    }

    private fun givenAnEmptyCache() {
        whenever(
            preferenceProvider.getObjectFromJson(eq(Preference.LOT_NUMBERS_CACHE), any<TypeToken<LotNumbersApiResponse>>(), any()),
        ) doReturn emptyMap()
    }

    private fun givenNoProduct() {
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityDataValues()
                .value(eventUid, PRODUCT_DE_UID)
                .blockingGet(),
        ) doReturn null
    }

    private fun givenAProduct(code: String) {
        val dataValue: TrackedEntityDataValue = mock()
        whenever(dataValue.value()) doReturn code
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityDataValues()
                .value(eventUid, PRODUCT_DE_UID)
                .blockingGet(),
        ) doReturn dataValue
    }

    private suspend fun givenRemoteData(data: LotNumbersApiResponse) {
        whenever(lotNumbersApi.getData()) doReturn data
    }

    private suspend fun givenNetworkError() {
        whenever(lotNumbersApi.getData()) doThrow RuntimeException("network error")
    }

    private fun givenCachedData(data: LotNumbersApiResponse) {
        whenever(
            preferenceProvider.getObjectFromJson(eq(Preference.LOT_NUMBERS_CACHE), any<TypeToken<LotNumbersApiResponse>>(), any()),
        ) doReturn data
    }
}
