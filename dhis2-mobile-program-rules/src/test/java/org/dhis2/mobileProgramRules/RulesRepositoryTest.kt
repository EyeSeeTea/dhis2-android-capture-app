package org.dhis2.mobileProgramRules

import kotlinx.coroutines.runBlocking
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.filters.internal.BooleanFilterConnector
import org.hisp.dhis.android.core.arch.repositories.filters.internal.DateFilterConnector
import org.hisp.dhis.android.core.arch.repositories.filters.internal.StringFilterConnector
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.event.internal.EventStatusFilterConnector
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitCollectionRepository
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitGroup
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramStageCollectionRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

class RulesRepositoryTest {
    private lateinit var repository: RulesRepository

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    @Before
    fun setUp() {
        repository = RulesRepository(d2)
    }

    @Test
    fun `Should load supplementary data`() {
        whenever(
            d2
                .organisationUnitModule()
                .organisationUnits()
                .withOrganisationUnitGroups()
                .uid("org_unit_test")
                .blockingGet(),
        ) doReturn getTestOrgUnit()
        whenever(d2.userModule().userRoles().blockingGetUids()) doReturn getTestUserRoles()
        whenever(d2.userModule().userGroups().blockingGetUids()) doReturn getTestUserGroups()

        val supplData =
            runBlocking {
                repository.supplementaryData("org_unit_test")
            }
        assertTrue(supplData.isNotEmpty())
        assertTrue(supplData.containsKey("USER_ROLES"))
        assertTrue(supplData.containsKey("USER_GROUPS"))
        assertTrue(supplData.containsKey("org_unit_group_test_code"))
        assertTrue(supplData.containsKey("org_unit_group_test"))
        assertTrue(supplData["USER_ROLES"]?.contains("UtXToHNI0Cb") ?: false)
        assertTrue(supplData["USER_ROLES"]?.contains("oPNOIj7zJ1m") ?: false)
        assertTrue(supplData["USER_GROUPS"]?.contains("gVC8vCfNAx8") ?: false)
        assertTrue(supplData["USER_GROUPS"]?.contains("Kk12LkEWtXp") ?: false)
        assertTrue(
            supplData
                .getOrElse("org_unit_group_test") { arrayListOf() }
                .contains("org_unit_test"),
        )
        assertTrue(
            supplData
                .getOrElse("org_unit_group_test_code") { arrayListOf() }
                .contains("org_unit_test"),
        )
    }

    @Test
    fun `Supplementary data should not include option groups with null code`() {
        whenever(
            d2
                .organisationUnitModule()
                .organisationUnits()
                .withOrganisationUnitGroups()
                .uid("org_unit_test")
                .blockingGet(),
        ) doReturn getTestOrgUnitWithNullCodeGroup()
        whenever(d2.userModule().userRoles().blockingGetUids()) doReturn getTestUserRoles()
        whenever(d2.userModule().userGroups().blockingGetUids()) doReturn getTestUserGroups()

        val supplData =
            runBlocking {
                repository.supplementaryData("org_unit_test")
            }

        assertTrue(supplData.isNotEmpty())

        assertTrue(supplData.containsKey("USER_ROLES"))
        assertTrue(!supplData.containsKey("org_unit_group_test_code"))
        assertTrue(supplData.containsKey("org_unit_group_test"))
        assertTrue(supplData["USER_ROLES"]?.contains("UtXToHNI0Cb") ?: false)
        assertTrue(supplData["USER_ROLES"]?.contains("oPNOIj7zJ1m") ?: false)
        assertTrue(
            supplData
                .getOrElse("org_unit_group_test") { arrayListOf() }
                .contains("org_unit_test"),
        )
        assertTrue(supplData.getOrElse("org_unit_group_test_code") { arrayListOf() }.isEmpty())
    }

    // EyeSeeTea customization - rule engine bulk context
    @Test
    fun `Enrollment events should resolve stage names and org unit codes via bulk lookups`() {
        // The SDK's fluent filters (eq/notIn/isFalse/in) return generic types, so each
        // hop needs an explicit mock — deep stubs cannot recover the concrete repository.
        val byEnrollment: StringFilterConnector<EventCollectionRepository> = mock()
        val afterEnrollment: EventCollectionRepository = mock()
        val byStatus: EventStatusFilterConnector = mock()
        val afterStatus: EventCollectionRepository = mock()
        val byEventDate: DateFilterConnector<EventCollectionRepository> = mock()
        val afterEventDate: EventCollectionRepository = mock()
        val byDeleted: BooleanFilterConnector<EventCollectionRepository> = mock()
        val afterDeleted: EventCollectionRepository = mock()
        whenever(d2.eventModule().events().byEnrollmentUid()) doReturn byEnrollment
        whenever(byEnrollment.eq("enrollment_uid")) doReturn afterEnrollment
        whenever(afterEnrollment.byStatus()) doReturn byStatus
        whenever(
            byStatus.notIn(EventStatus.SCHEDULE, EventStatus.SKIPPED, EventStatus.OVERDUE),
        ) doReturn afterStatus
        whenever(afterStatus.byEventDate()) doReturn byEventDate
        whenever(byEventDate.beforeOrEqual(any())) doReturn afterEventDate
        whenever(afterEventDate.byDeleted()) doReturn byDeleted
        whenever(byDeleted.isFalse) doReturn afterDeleted
        whenever(afterDeleted.withTrackedEntityDataValues()) doReturn afterDeleted
        whenever(afterDeleted.blockingGet()) doReturn
            listOf(getTestEvent("event_1", "stage_1", "ou_1"), getTestEvent("event_2", "stage_2", "ou_1"))

        val stagesByUid: StringFilterConnector<ProgramStageCollectionRepository> = mock()
        val stagesRepo: ProgramStageCollectionRepository = mock()
        whenever(d2.programModule().programStages().byUid()) doReturn stagesByUid
        whenever(stagesByUid.`in`(listOf("stage_1", "stage_2"))) doReturn stagesRepo
        whenever(stagesRepo.blockingGet()) doReturn
            listOf(
                ProgramStage
                    .builder()
                    .uid("stage_1")
                    .name("Stage one")
                    .build(),
                ProgramStage
                    .builder()
                    .uid("stage_2")
                    .name("Stage two")
                    .build(),
            )

        val orgUnitsByUid: StringFilterConnector<OrganisationUnitCollectionRepository> = mock()
        val orgUnitsRepo: OrganisationUnitCollectionRepository = mock()
        whenever(d2.organisationUnitModule().organisationUnits().byUid()) doReturn orgUnitsByUid
        whenever(orgUnitsByUid.`in`(listOf("ou_1"))) doReturn orgUnitsRepo
        whenever(orgUnitsRepo.blockingGet()) doReturn
            listOf(
                OrganisationUnit
                    .builder()
                    .uid("ou_1")
                    .code("OU1")
                    .build(),
            )

        val ruleEvents =
            runBlocking {
                repository.enrollmentEvents("enrollment_uid")
            }

        assertEquals(2, ruleEvents.size)
        assertEquals("Stage one", ruleEvents[0].programStageName)
        assertEquals("Stage two", ruleEvents[1].programStageName)
        assertEquals("OU1", ruleEvents[0].organisationUnitCode)
        assertEquals("OU1", ruleEvents[1].organisationUnitCode)
        assertEquals("event_1", ruleEvents[0].event)
        assertEquals("event_2", ruleEvents[1].event)
    }

    private fun getTestEvent(
        uid: String,
        stageUid: String,
        orgUnitUid: String,
    ): Event =
        Event
            .builder()
            .uid(uid)
            .programStage(stageUid)
            .organisationUnit(orgUnitUid)
            .status(EventStatus.ACTIVE)
            .eventDate(Date())
            .build()

    private fun getTestUserRoles(): List<String> = arrayListOf("UtXToHNI0Cb", "oPNOIj7zJ1m")

    private fun getTestUserGroups(): List<String> = listOf("gVC8vCfNAx8", "Kk12LkEWtXp")

    private fun getTestOrgUnit(): OrganisationUnit =
        OrganisationUnit
            .builder()
            .uid("org_unit_test")
            .organisationUnitGroups(arrayListOf(getTestOrgUnitGroup("org_unit_group_test_code")))
            .build()

    private fun getTestOrgUnitWithNullCodeGroup(): OrganisationUnit =
        OrganisationUnit
            .builder()
            .uid("org_unit_test")
            .organisationUnitGroups(arrayListOf(getTestOrgUnitGroup()))
            .build()

    private fun getTestOrgUnitGroup(ouCode: String? = null): OrganisationUnitGroup? =
        OrganisationUnitGroup
            .builder()
            .uid("org_unit_group_test")
            .code(ouCode)
            .build()
}
