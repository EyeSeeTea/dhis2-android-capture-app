package org.dhis2.usescases.receipt.data

import kotlinx.coroutines.test.runTest
import java.util.Date
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.dhis2.usescases.receipt.domain.model.GeographicalLocation
import org.dhis2.usescases.receipt.domain.model.ReceiptItem
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
    fun `the receipt shows the distribution name instead of its option code, the SPBH id and the beneficiary name`() = runTest {
        givenADistributionEvent(
            eventUid = EVENT_UID,
            distributionCode = "D0001",
            distributionName = "TEST Distribution 1",
            spbhId = "SPBH-0042",
            beneficiaryName = "Ana Perez",
        )
        val repository = givenAReceiptRepository()

        val receipt = repository.getReceipt(EVENT_UID)

        assertEquals("TEST Distribution 1", receipt?.distributionName)
        assertEquals("SPBH-0042", receipt?.spbhId)
        assertEquals("Ana Perez", receipt?.beneficiaryName)
    }

    @Test
    fun `the receipt lists only the items given with a value, chosen by their data element code, with label and quantity`() = runTest {
        givenAnEventWithDataValues(
            eventUid = EVENT_UID,
            dataValues = listOf(
                givenADataValue(code = "sp_DIST_RICE_GIVEN", label = "Rice (kg) given", value = "5"),
                givenADataValue(code = "sp_DIST_OIL_GIVEN", label = "Oil (l) given", value = ""),
                givenADataValue(code = "sp_DIST_SALT_GIVEN", label = "Salt (kg) given", value = "2"),
                givenADataValue(code = "sp_DIST_NOTES", label = "Notes", value = "7"),
            ),
        )
        val repository = givenAReceiptRepository()

        val receipt = repository.getReceipt(EVENT_UID)

        assertEquals(
            listOf(ReceiptItem(label = "Rice (kg) given", quantity = "5"), ReceiptItem(label = "Salt (kg) given", quantity = "2")),
            receipt?.items,
        )
    }

    @Test
    fun `an other food slot is listed with the name assigned in the event as its label`() = runTest {
        givenAnEventWithDataValues(
            eventUid = EVENT_UID,
            dataValues = givenAnOtherSlot(kind = "FOOD", number = 1, description = "Dates", quantity = "3"),
        )
        val repository = givenAReceiptRepository()

        val receipt = repository.getReceipt(EVENT_UID)

        assertEquals(listOf(ReceiptItem(label = "Dates", quantity = "3")), receipt?.items)
    }

    @Test
    fun `an other item slot is listed with the name assigned in the event as its label`() = runTest {
        givenAnEventWithDataValues(
            eventUid = EVENT_UID,
            dataValues = givenAnOtherSlot(kind = "ITEM", number = 2, description = "Blankets", quantity = "4"),
        )
        val repository = givenAReceiptRepository()

        val receipt = repository.getReceipt(EVENT_UID)

        assertEquals(listOf(ReceiptItem(label = "Blankets", quantity = "4")), receipt?.items)
    }

    @Test
    fun `each other slot takes the name of its own description`() = runTest {
        givenAnEventWithDataValues(
            eventUid = EVENT_UID,
            dataValues = givenAnOtherSlot(kind = "FOOD", number = 1, description = "Dates", quantity = "3") +
                givenAnOtherSlot(kind = "FOOD", number = 2, description = "Lentils", quantity = "5") +
                givenAnOtherSlot(kind = "ITEM", number = 1, description = "Blankets", quantity = "4"),
        )
        val repository = givenAReceiptRepository()

        val receipt = repository.getReceipt(EVENT_UID)

        assertEquals(
            listOf(
                ReceiptItem(label = "Dates", quantity = "3"),
                ReceiptItem(label = "Lentils", quantity = "5"),
                ReceiptItem(label = "Blankets", quantity = "4"),
            ),
            receipt?.items,
        )
    }

    @Test
    fun `an other slot with a quantity but no description is listed with its generic label`() = runTest {
        givenAnEventWithDataValues(
            eventUid = EVENT_UID,
            dataValues = givenAnOtherSlot(kind = "FOOD", number = 1, description = null, quantity = "3"),
        )
        val repository = givenAReceiptRepository()

        val receipt = repository.getReceipt(EVENT_UID)

        assertEquals(listOf(ReceiptItem(label = "Other food 1 given", quantity = "3")), receipt?.items)
    }

    @Test
    fun `an other slot with a description but no quantity is omitted and its description is not listed as an item`() = runTest {
        givenAnEventWithDataValues(
            eventUid = EVENT_UID,
            dataValues = listOf(givenADataValue(code = "sp_DIST_RICE_GIVEN", label = "Rice (kg) given", value = "5")) +
                givenAnOtherSlot(kind = "FOOD", number = 1, description = "Dates", quantity = null),
        )
        val repository = givenAReceiptRepository()

        val receipt = repository.getReceipt(EVENT_UID)

        assertEquals(listOf(ReceiptItem(label = "Rice (kg) given", quantity = "5")), receipt?.items)
    }

    // DHIS2 does not store an empty value: a missing description or quantity is a data value that does not exist.
    private fun givenAnOtherSlot(kind: String, number: Int, description: String?, quantity: String?): List<DataValueFixture> {
        val prefix = "sp_DIST_OTHER_${kind}_$number"
        val slot = kind.lowercase()
        return listOfNotNull(
            description?.let {
                givenADataValue(code = "${prefix}_DESC", label = "Other $slot $number description", value = it, valueType = ValueType.TEXT)
            },
            quantity?.let { givenADataValue(code = "${prefix}_GIVEN", label = "Other $slot $number given", value = it) },
        )
    }

    @Test
    fun `the receipt shows the name of the organisation unit of the event as the distribution point`() = runTest {
        givenAnEventInOrgUnit(eventUid = EVENT_UID, orgUnitUid = "ORG_UNIT_UID", orgUnitName = "Gethy distribution point")
        givenAnEventWithDataValues(eventUid = EVENT_UID, dataValues = emptyList())
        val repository = givenAReceiptRepository()

        val receipt = repository.getReceipt(EVENT_UID)

        assertEquals("Gethy distribution point", receipt?.distributionPoint)
    }

    @Test
    fun `the receipt shows the date of the event as the distribution date`() = runTest {
        val eventDate = Date(1_763_251_200_000L)
        givenAnEventOnDate(eventUid = EVENT_UID, eventDate = eventDate)
        givenAnEventWithDataValues(eventUid = EVENT_UID, dataValues = emptyList())
        val repository = givenAReceiptRepository()

        val receipt = repository.getReceipt(EVENT_UID)

        assertEquals(eventDate, receipt?.distributionDate)
    }

    @Test
    fun `the receipt shows the three geographical locations of the beneficiary in level order`() = runTest {
        givenAnEventOfTrackedEntity(eventUid = EVENT_UID)
        givenAnEventWithDataValues(eventUid = EVENT_UID, dataValues = emptyList())
        givenAnAttributeValue(GEO_LEVEL_1_UID, "Gethy Region")
        givenAnAttributeValue(GEO_LEVEL_2_UID, "North District")
        givenAnAttributeValue(GEO_LEVEL_3_UID, "Hill Village")
        val repository = givenAReceiptRepository()

        val receipt = repository.getReceipt(EVENT_UID)

        assertEquals(
            listOf(
                GeographicalLocation(level = 1, name = "Gethy Region"),
                GeographicalLocation(level = 2, name = "North District"),
                GeographicalLocation(level = 3, name = "Hill Village"),
            ),
            receipt?.geographicalLocations,
        )
    }

    @Test
    fun `a geographical location level the beneficiary does not have is left out and the others keep their level`() = runTest {
        givenAnEventOfTrackedEntity(eventUid = EVENT_UID)
        givenAnEventWithDataValues(eventUid = EVENT_UID, dataValues = emptyList())
        givenAnAttributeValue(GEO_LEVEL_1_UID, "Gethy Region")
        givenNoAttributeValue(GEO_LEVEL_2_UID)
        givenAnAttributeValue(GEO_LEVEL_3_UID, "Hill Village")
        val repository = givenAReceiptRepository()

        val receipt = repository.getReceipt(EVENT_UID)

        assertEquals(
            listOf(
                GeographicalLocation(level = 1, name = "Gethy Region"),
                GeographicalLocation(level = 3, name = "Hill Village"),
            ),
            receipt?.geographicalLocations,
        )
    }

    private class DataValueFixture(val dataValue: TrackedEntityDataValue, val dataElement: DataElement)

    private fun givenADataValue(
        code: String,
        label: String,
        value: String,
        valueType: ValueType = ValueType.INTEGER_POSITIVE,
    ): DataValueFixture {
        val dataElementUid = "DE_$code"
        return DataValueFixture(
            dataValue = TrackedEntityDataValue.builder().event(EVENT_UID).dataElement(dataElementUid).value(value).build(),
            dataElement = DataElement.builder()
                .uid(dataElementUid)
                .code(code)
                .displayFormName(label)
                .valueType(valueType)
                .build(),
        )
    }

    private fun givenAnEventInOrgUnit(eventUid: String, orgUnitUid: String, orgUnitName: String) {
        whenever(d2.eventModule().events().uid(eventUid).blockingGet()) doReturn
            Event.builder().uid(eventUid).organisationUnit(orgUnitUid).build()
        whenever(d2.organisationUnitModule().organisationUnits().uid(orgUnitUid).blockingGet()) doReturn
            OrganisationUnit.builder().uid(orgUnitUid).displayName(orgUnitName).build()
    }

    private fun givenAnEventOnDate(eventUid: String, eventDate: Date) {
        whenever(d2.eventModule().events().uid(eventUid).blockingGet()) doReturn
            Event.builder().uid(eventUid).eventDate(eventDate).build()
    }

    private fun givenAnEventOfTrackedEntity(eventUid: String) {
        whenever(d2.eventModule().events().uid(eventUid).blockingGet()) doReturn
            Event.builder().uid(eventUid).enrollment(ENROLLMENT_UID).build()
        whenever(d2.enrollmentModule().enrollments().uid(ENROLLMENT_UID).blockingGet()) doReturn
            Enrollment.builder().uid(ENROLLMENT_UID).trackedEntityInstance(TEI_UID).build()
    }

    private fun givenAnEventWithDataValues(eventUid: String, dataValues: List<DataValueFixture>) {
        whenever(d2.trackedEntityModule().trackedEntityDataValues().byEvent()) doReturn mock()
        whenever(d2.trackedEntityModule().trackedEntityDataValues().byEvent().eq(eventUid)) doReturn mock()
        whenever(d2.trackedEntityModule().trackedEntityDataValues().byEvent().eq(eventUid).blockingGet()) doReturn
            dataValues.map { it.dataValue }
        dataValues.forEach {
            whenever(d2.dataElementModule().dataElements().uid(it.dataElement.uid()).blockingGet()) doReturn it.dataElement
        }
    }

    private fun givenAReceiptRepository() =
        D2ReceiptRepository(
            d2 = d2,
            metadata = ReceiptMetadata(
                distributionDataElementUid = DISTRIBUTION_DATA_ELEMENT_UID,
                spbhIdAttributeUid = SPBH_ID_ATTRIBUTE_UID,
                beneficiaryNameAttributeUid = BENEFICIARY_NAME_ATTRIBUTE_UID,
                geographicalLocationAttributeUids = listOf(GEO_LEVEL_1_UID, GEO_LEVEL_2_UID, GEO_LEVEL_3_UID),
            ),
        )

    private fun givenADistributionEvent(
        eventUid: String,
        distributionCode: String,
        distributionName: String,
        spbhId: String,
        beneficiaryName: String,
    ) {
        whenever(d2.eventModule().events().uid(eventUid).blockingGet()) doReturn
            Event.builder().uid(eventUid).enrollment(ENROLLMENT_UID).build()
        whenever(d2.enrollmentModule().enrollments().uid(ENROLLMENT_UID).blockingGet()) doReturn
            Enrollment.builder().uid(ENROLLMENT_UID).trackedEntityInstance(TEI_UID).build()

        givenNoDataValuesToList(eventUid)
        givenADistributionDataValue(eventUid, distributionCode, distributionName)
        givenAnAttributeValue(SPBH_ID_ATTRIBUTE_UID, spbhId)
        givenAnAttributeValue(BENEFICIARY_NAME_ATTRIBUTE_UID, beneficiaryName)
    }

    // The repository reads the data values of the event to pick the items given: this event has none.
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

    private fun givenNoAttributeValue(attributeUid: String) {
        whenever(d2.trackedEntityModule().trackedEntityAttributeValues().value(attributeUid, TEI_UID).blockingGet()) doReturn null
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
        const val SPBH_ID_ATTRIBUTE_UID = "SPBH_ID_ATTRIBUTE_UID"
        const val BENEFICIARY_NAME_ATTRIBUTE_UID = "BENEFICIARY_NAME_ATTRIBUTE_UID"
        const val GEO_LEVEL_1_UID = "GEO_LEVEL_1_UID"
        const val GEO_LEVEL_2_UID = "GEO_LEVEL_2_UID"
        const val GEO_LEVEL_3_UID = "GEO_LEVEL_3_UID"
    }
}
