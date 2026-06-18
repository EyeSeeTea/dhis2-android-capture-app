package org.dhis2.form.data.lotnumber

// EyeSeeTea customization - Lot Number Search Field

import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.form.model.lotnumber.LotNumberRepository
import org.dhis2.form.model.lotnumber.LotNumbersResult
import org.hisp.dhis.android.core.D2

class LotNumberD2Repository(
    private val d2: D2,
    private val preferenceProvider: PreferenceProvider,
    private val lotNumbersApi: LotNumbersApi,
    // Inject the dispatcher so the repository is main-safe and the unit test can drive it deterministically.
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : LotNumberRepository {
    override suspend fun getLotNumbers(eventUid: String): LotNumbersResult =
        // D2 blockingGet, the synchronous HTTP call and the Gson cache read all block: move them off Main.
        withContext(ioDispatcher) {
            val productCode = resolveProductCode(eventUid) ?: return@withContext LotNumbersResult.NoProductSelected
            val orgUnitCode = resolveOrgUnitCode(eventUid) ?: return@withContext LotNumbersResult.NotFound

            val data = fetchFromRemoteAndCache() ?: readFromCache()
            val lotNumbers = data[orgUnitCode]?.get(productCode)?.lotNumbers ?: emptyList()

            if (lotNumbers.isEmpty()) LotNumbersResult.NotFound else LotNumbersResult.Available(lotNumbers)
        }

    override suspend fun refreshCache() {
        withContext(ioDispatcher) {
            fetchFromRemoteAndCache()
        }
    }

    private fun resolveProductCode(eventUid: String): String? =
        d2
            .trackedEntityModule()
            .trackedEntityDataValues()
            .value(eventUid, PRODUCT_DE_UID)
            .blockingGet()
            ?.value()
            ?.takeIf { it.isNotBlank() }

    private fun resolveOrgUnitCode(eventUid: String): String? {
        val orgUnitUid =
            d2
                .eventModule()
                .events()
                .uid(eventUid)
                .blockingGet()
                ?.organisationUnit() ?: return null
        return d2
            .organisationUnitModule()
            .organisationUnits()
            .uid(orgUnitUid)
            .blockingGet()
            ?.code()
    }

    private suspend fun fetchFromRemoteAndCache(): LotNumbersApiResponse? =
        try {
            lotNumbersApi.getData().also { saveToCache(it) }
        } catch (e: Exception) {
            null
        }

    private fun saveToCache(data: LotNumbersApiResponse) {
        preferenceProvider.saveAsJson(Preference.LOT_NUMBERS_CACHE, data)
    }

    private fun readFromCache(): LotNumbersApiResponse {
        val typeToken = object : TypeToken<LotNumbersApiResponse>() {}
        return preferenceProvider.getObjectFromJson(Preference.LOT_NUMBERS_CACHE, typeToken, emptyMap())
    }
}
