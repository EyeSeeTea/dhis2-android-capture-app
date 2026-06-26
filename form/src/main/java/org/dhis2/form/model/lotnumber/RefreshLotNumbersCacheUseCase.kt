package org.dhis2.form.model.lotnumber

// EyeSeeTea customization - Lot Number Search Field

class RefreshLotNumbersCacheUseCase(
    private val lotNumberRepository: LotNumberRepository,
) {
    suspend operator fun invoke() = lotNumberRepository.refreshCache()
}
