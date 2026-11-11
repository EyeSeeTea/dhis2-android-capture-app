package org.dhis2.usescases.receipt.data

import kotlinx.coroutines.test.runTest
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

// The SDK is the outer boundary of this test: D2 is doubled, the repository and the Oslo value helpers run for real.
class D2ReceiptRepositoryTest {

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    @Test
    fun `the receipt shows the distribution name instead of its option code, the ration card id and the beneficiary name`() = runTest {
        givenADistributionEvent(
            eventUid = EVENT_UID,
            distributionCode = "D0001",
            distributionName = "TEST Distribution 1",
            rationCardId = "RC-0042",
            beneficiaryName = "Ana Perez",
        )
        val repository = givenAReceiptRepository()

        val receipt = repository.getReceipt(EVENT_UID)

        assertEquals("TEST Distribution 1", receipt?.distributionName)
        assertEquals("RC-0042", receipt?.rationCardId)
        assertEquals("Ana Perez", receipt?.beneficiaryName)
    }

    private fun givenAReceiptRepository() =
        D2ReceiptRepository(
            d2 = d2,
            metadata = ReceiptMetadata(
                distributionDataElementUid = DISTRIBUTION_DATA_ELEMENT_UID,
                rationCardIdAttributeUid = RATION_CARD_ATTRIBUTE_UID,
                beneficiaryNameAttributeUid = BENEFICIARY_NAME_ATTRIBUTE_UID,
            ),
        )

    private fun givenADistributionEvent(
        eventUid: String,
        distributionCode: String,
        distributionName: String,
        rationCardId: String,
        beneficiaryName: String,
    ) {
        whenever(d2.eventModule().events().uid(eventUid).blockingGet()) doReturn
            Event.builder().uid(eventUid).enrollment(ENROLLMENT_UID).build()
        whenever(d2.enrollmentModule().enrollments().uid(ENROLLMENT_UID).blockingGet()) doReturn
            Enrollment.builder().uid(ENROLLMENT_UID).trackedEntityInstance(TEI_UID).build()

        givenNoDataValuesToList(eventUid)
        givenADistributionDataValue(eventUid, distributionCode, distributionName)
        givenAnAttributeValue(RATION_CARD_ATTRIBUTE_UID, rationCardId)
        givenAnAttributeValue(BENEFICIARY_NAME_ATTRIBUTE_UID, beneficiaryName)
    }

    // The repository still lists every data value of the event (generic lines, replaced by tasks 5.6-5.8).
    private fun givenNoDataValuesToList(eventUid: String) {
        whenever(d2.trackedEntityModule().trackedEntityDataValues().byEvent()) doReturn mock()
        whenever(d2.trackedEntityModule().trackedEntityDataValues().byEvent().eq(eventUid)) doReturn mock()
        whenever(d2.trackedEntityModule().trackedEntityDataValues().byEvent().eq(eventUid).blockingGet()) doReturn
            emptyList()
    }

    private fun givenADistributionDataValue(eventUid: String, code: String, name: String) {
        whenever(
            d2.trackedEntityModule().trackedEntityDataValues()
                .value(eventUid, DISTRIBUTION_DATA_ELEMENT_UID).blockingGet(),
        ) doReturn TrackedEntityDataValue.builder()
            .event(eventUid)
            .dataElement(DISTRIBUTION_DATA_ELEMENT_UID)
            .value(code)
            .build()
        whenever(d2.dataElementModule().dataElements().uid(DISTRIBUTION_DATA_ELEMENT_UID).blockingGet()) doReturn
            DataElement.builder()
                .uid(DISTRIBUTION_DATA_ELEMENT_UID)
                .valueType(ValueType.TEXT)
                .optionSet(ObjectWithUid.create(DISTRIBUTION_OPTION_SET_UID))
                .build()
        // One stub per link with an explicit mock(): deep stubs cannot infer the generic types of this SDK chain
        // (same approach as OptionsRepositoryTest).
        whenever(d2.optionModule().options()) doReturn mock()
        whenever(d2.optionModule().options().byOptionSetUid()) doReturn mock()
        whenever(d2.optionModule().options().byOptionSetUid().eq(DISTRIBUTION_OPTION_SET_UID)) doReturn mock()
        whenever(d2.optionModule().options().byOptionSetUid().eq(DISTRIBUTION_OPTION_SET_UID).byCode()) doReturn mock()
        whenever(
            d2.optionModule().options().byOptionSetUid().eq(DISTRIBUTION_OPTION_SET_UID).byCode().eq(code),
        ) doReturn mock()
        whenever(
            d2.optionModule().options().byOptionSetUid().eq(DISTRIBUTION_OPTION_SET_UID).byCode().eq(code).one(),
        ) doReturn mock()
        // The helper also checks whether the value matches an option display name: it does not (the value is the code).
        whenever(d2.optionModule().options().byOptionSetUid().eq(DISTRIBUTION_OPTION_SET_UID).byDisplayName()) doReturn
            mock()
        whenever(
            d2.optionModule().options().byOptionSetUid().eq(DISTRIBUTION_OPTION_SET_UID).byDisplayName().eq(code),
        ) doReturn mock()
        whenever(
            d2.optionModule().options().byOptionSetUid().eq(DISTRIBUTION_OPTION_SET_UID).byDisplayName().eq(code).one(),
        ) doReturn mock()
        whenever(
            d2.optionModule().options().byOptionSetUid().eq(DISTRIBUTION_OPTION_SET_UID).byCode().eq(code)
                .one().blockingExists(),
        ) doReturn true
        whenever(
            d2.optionModule().options().byOptionSetUid().eq(DISTRIBUTION_OPTION_SET_UID).byCode().eq(code)
                .one().blockingGet(),
        ) doReturn Option.builder().uid("OPTION_UID").code(code).name(name).displayName(name).build()
    }

    private fun givenAnAttributeValue(attributeUid: String, value: String) {
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues().value(attributeUid, TEI_UID).blockingGet(),
        ) doReturn TrackedEntityAttributeValue.builder()
            .trackedEntityAttribute(attributeUid)
            .trackedEntityInstance(TEI_UID)
            .value(value)
            .build()
        whenever(d2.trackedEntityModule().trackedEntityAttributes().uid(attributeUid).blockingGet()) doReturn
            TrackedEntityAttribute.builder().uid(attributeUid).valueType(ValueType.TEXT).build()
    }

    private companion object {
        const val EVENT_UID = "EVENT_UID"
        const val ENROLLMENT_UID = "ENROLLMENT_UID"
        const val TEI_UID = "TEI_UID"
        const val DISTRIBUTION_DATA_ELEMENT_UID = "DISTRIBUTION_DE_UID"
        const val DISTRIBUTION_OPTION_SET_UID = "DISTRIBUTION_OPTION_SET_UID"
        const val RATION_CARD_ATTRIBUTE_UID = "RATION_CARD_ATTRIBUTE_UID"
        const val BENEFICIARY_NAME_ATTRIBUTE_UID = "BENEFICIARY_NAME_ATTRIBUTE_UID"
    }
}
