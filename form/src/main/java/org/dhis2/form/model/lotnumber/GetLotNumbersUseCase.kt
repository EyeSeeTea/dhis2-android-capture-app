package org.dhis2.form.model.lotnumber

// EyeSeeTea customization - Lot Number Search Field

sealed class LotNumbersResult {
    object NoProductSelected : LotNumbersResult()

    object NotFound : LotNumbersResult()

    data class Available(
        val lotNumbers: List<String>,
    ) : LotNumbersResult()
}

class GetLotNumbersUseCase(
    private val lotNumberRepository: LotNumberRepository,
) {
    suspend operator fun invoke(eventUid: String): LotNumbersResult = lotNumberRepository.getLotNumbers(eventUid)
}
