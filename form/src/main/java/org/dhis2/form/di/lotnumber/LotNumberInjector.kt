package org.dhis2.form.di.lotnumber

import android.content.Context
import org.dhis2.commons.prefs.PreferenceProviderImpl
import org.dhis2.form.data.lotnumber.LotNumberD2Repository
import org.dhis2.form.data.lotnumber.LotNumbersApi
import org.dhis2.form.model.lotnumber.GetLotNumbersUseCase
import org.dhis2.form.ui.dialog.lotnumber.LotNumberDialogViewModel
import org.hisp.dhis.android.core.D2Manager

// EyeSeeTea customization - Lot Number Search Field

object LotNumberInjector {
    private fun provideD2() = D2Manager.getD2()

    fun provideLotNumberD2Repository(context: Context): LotNumberD2Repository {
        val d2 = provideD2()
        return LotNumberD2Repository(
            d2 = d2,
            preferenceProvider = PreferenceProviderImpl(context),
            lotNumbersApi = LotNumbersApi(d2.httpServiceClient()),
        )
    }

    fun provideGetLotNumbersUseCase(context: Context): GetLotNumbersUseCase =
        GetLotNumbersUseCase(
            lotNumberRepository = provideLotNumberD2Repository(context),
        )

    fun provideLotNumberDialogViewModelFactory(
        context: Context,
        eventUid: String,
    ): LotNumberDialogViewModel.Factory =
        LotNumberDialogViewModel.Factory(
            eventUid = eventUid,
            getLotNumbers = provideGetLotNumbersUseCase(context),
        )
}
