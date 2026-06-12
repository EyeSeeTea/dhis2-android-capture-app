package org.dhis2.mobileProgramRules

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.dhis2.commons.rules.RuleEngineContextData
import org.hisp.dhis.rules.api.RuleEngine
import org.hisp.dhis.rules.api.RuleEngineContext
import org.hisp.dhis.rules.models.RuleEffect
import org.hisp.dhis.rules.models.RuleEnrollment
import org.hisp.dhis.rules.models.RuleEvent

class RuleEngineHelper(
    private val evaluationType: EvaluationType,
    private val rulesRepository: RulesRepository,
) {
    private val ruleEngine by lazy { RuleEngine.getInstance() }
    private lateinit var contextData: RuleEngineContextData
    private var refreshContext: Boolean = false

    // EyeSeeTea customization - rule engine bulk context
    // Separate flag for the evaluation target (cheap single-entity queries) so the
    // expensive context rebuild can clear its own flag under the mutex without
    // suppressing the target refresh.
    private var refreshTarget: Boolean = false
    private val contextMutex = Mutex()
    private lateinit var targetEnrollment: RuleEnrollment
    private lateinit var targetEvent: RuleEvent

    fun evaluate(): List<RuleEffect> {
        var ruleEffects = emptyList<RuleEffect>()
        runBlocking {
            async { buildRuleEngineContextData(evaluationType.targetUid) }.await()
            ruleEffects =
                async {
                    when (evaluationType) {
                        is EvaluationType.Enrollment ->
                            ruleEngine.evaluate(
                                target =
                                    buildTargetEnrollment(evaluationType.targetUid).copy(
                                        attributeValues =
                                            rulesRepository.queryAttributeValues(
                                                evaluationType.targetUid,
                                            ),
                                    ),
                                ruleEvents = contextData.ruleEvents,
                                executionContext = contextData.ruleEngineContext,
                            )

                        is EvaluationType.Event ->
                            ruleEngine.evaluate(
                                target =
                                    buildTargetEvent(evaluationType.targetUid).copy(
                                        dataValues =
                                            rulesRepository.queryDataValues(
                                                evaluationType.targetUid,
                                            ),
                                    ),
                                ruleEnrollment = contextData.ruleEnrollment,
                                ruleEvents = contextData.ruleEvents,
                                executionContext = contextData.ruleEngineContext,
                            )
                    }
                }.await()
        }
        return ruleEffects
    }

    private suspend fun buildRuleEngineContextData(targetUid: String) {
        // EyeSeeTea customization - rule engine bulk context
        // Mutex guarantees a single in-flight context build: concurrent evaluations
        // wait here, re-check the condition, and reuse the freshly built context
        // instead of each rebuilding it (the build loads every enrollment event).
        contextMutex.withLock {
            if (::contextData.isInitialized.not() || refreshContext) {
                val (programUid, orgUnitUid) = getProgramAndOrgUnit(targetUid)

                coroutineScope {
                    val rules =
                        async {
                            rulesRepository.rules(
                                programUid = programUid,
                                eventUid = targetUid.takeIf { evaluationType !is EvaluationType.Enrollment },
                            )
                        }

                    val ruleVariables = async { rulesRepository.ruleVariables(programUid = programUid) }
                    val supplData = async { rulesRepository.supplementaryData(orgUnitUid = orgUnitUid) }
                    val constants = async { rulesRepository.constants() }
                    val ruleEnrollment = async { getRuleEnrollment(targetUid) }
                    val ruleEvents = async { getRuleEvents(targetUid) }

                    contextData =
                        RuleEngineContextData(
                            ruleEngineContext =
                                RuleEngineContext(
                                    rules = rules.await(),
                                    ruleVariables = ruleVariables.await(),
                                    supplementaryData = supplData.await(),
                                    constantsValues = constants.await(),
                                ),
                            ruleEnrollment = ruleEnrollment.await(),
                            ruleEvents = ruleEvents.await(),
                        )
                }
                refreshContext = false
            }
        }
    }

    private fun getProgramAndOrgUnit(targetUid: String) =
        if (evaluationType is EvaluationType.Enrollment) {
            rulesRepository.enrollmentProgram(enrollmentUid = targetUid)
        } else {
            rulesRepository.eventProgram(eventUid = targetUid)
        }

    private suspend fun getRuleEnrollment(targetUid: String) =
        if (evaluationType !is EvaluationType.Enrollment) {
            rulesRepository.enrollment(eventUid = targetUid)
        } else {
            null
        }

    private suspend fun getRuleEvents(targetUid: String) =
        when (evaluationType) {
            is EvaluationType.Enrollment ->
                rulesRepository.enrollmentEvents(
                    enrollmentUid = targetUid,
                )

            is EvaluationType.Event ->
                rulesRepository.otherEvents(
                    eventUidToEvaluate = targetUid,
                )
        }

    private fun buildTargetEnrollment(enrollmentUid: String): RuleEnrollment {
        if (::targetEnrollment.isInitialized.not() || refreshTarget) {
            targetEnrollment = rulesRepository.getRuleEnrollment(enrollmentUid)
        }

        refreshTarget = false
        return targetEnrollment
    }

    private fun buildTargetEvent(eventUid: String): RuleEvent {
        if (::targetEvent.isInitialized.not() || refreshTarget) {
            targetEvent = rulesRepository.getRuleEvent(eventUid)
        }

        refreshTarget = false
        return targetEvent
    }

    fun refreshContext() {
        refreshContext = true
        refreshTarget = true
    }
}
