package org.dhis2.data.user

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerUser
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.form.data.lotnumber.LotNumberD2Repository
import org.dhis2.form.data.lotnumber.LotNumbersApi
import org.dhis2.form.model.lotnumber.RefreshLotNumbersCacheUseCase
import org.hisp.dhis.android.core.D2

@Module
class UserModule {
    @Provides
    @PerUser
    fun userRepository(d2: D2?): UserRepository = UserRepositoryImpl(d2!!)

    // EyeSeeTea customization - Lot Number Search Field
    @Provides
    @PerUser
    fun refreshLotNumbersCacheUseCase(
        d2: D2,
        preferenceProvider: PreferenceProvider,
    ): RefreshLotNumbersCacheUseCase =
        RefreshLotNumbersCacheUseCase(
            LotNumberD2Repository(
                d2 = d2,
                preferenceProvider = preferenceProvider,
                lotNumbersApi = LotNumbersApi(d2.httpServiceClient()),
            ),
        )
}
