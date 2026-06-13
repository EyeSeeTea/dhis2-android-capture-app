package org.dhis2.usescases.teiDashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Hub
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.mobile.commons.coroutine.CoroutineTracker
import org.dhis2.tracker.NavigationBarUIState
import org.dhis2.tracker.TEIDashboardItems
import org.dhis2.tracker.relationships.ui.state.RelationshipTopBarIconState
import org.dhis2.utils.AuthorityException
import org.dhis2.utils.analytics.ACTIVE_FOLLOW_UP
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.FOLLOW_UP
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.dhis2.utils.isPortrait
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.State.SYNCED
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBarItem
import timber.log.Timber

class DashboardViewModel(
    private val repository: DashboardRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val dispatcher: DispatcherProvider,
    private val pageConfigurator: NavigationPageConfigurator,
    private val resourcesManager: ResourceManager,
) : ViewModel() {
    private val eventUid = MutableLiveData<String>()

    private val selectedEventUid = MutableLiveData<String>()

    val showStatusErrorMessages = MutableLiveData(StatusChangeResultCode.CHANGED)

    private var _showFollowUpBar = MutableStateFlow(false)
    val showFollowUpBar = _showFollowUpBar.asStateFlow()

    private var _showStatusBar = MutableStateFlow<EnrollmentStatus?>(null)
    val showStatusBar = _showStatusBar.asStateFlow()

    private val _syncNeeded = MutableStateFlow(false)
    val syncNeeded = _syncNeeded.asStateFlow()

    private var _state = MutableStateFlow<State?>(null)
    val state = _state.asStateFlow()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _dashboardModel = MutableLiveData<DashboardModel?>()
    var dashboardModel: LiveData<DashboardModel?> = _dashboardModel

    private val _groupByStage = MutableLiveData<Boolean>()
    val groupByStage: LiveData<Boolean> = _groupByStage

    private val _noEnrollmentSelected = MutableLiveData(false)
    val noEnrollmentSelected: LiveData<Boolean> = _noEnrollmentSelected

    private val _navigationBarUIState =
        MutableStateFlow<NavigationBarUIState<TEIDashboardItems>>(NavigationBarUIState())
    val navigationBarUIState = _navigationBarUIState.asStateFlow()

    private val _relationshipTopBarIconState =
        MutableStateFlow<RelationshipTopBarIconState>(RelationshipTopBarIconState.List())
    val relationshipTopBarIconState = _relationshipTopBarIconState.asStateFlow()

    init {
        fetchDashboardModel()
        fetchGrouping()
    }

    private fun fetchDashboardModel() {
        viewModelScope.launch(dispatcher.io()) {
            CoroutineTracker.increment()
            val result =
                async {
                    repository.getDashboardModel()
                }
            try {
                val model = result.await()
                _dashboardModel.postValue(model)
                if (model is DashboardEnrollmentModel) {
                    _showFollowUpBar.value =
                        model.currentEnrollment.followUp() ?: false
                    _syncNeeded.value =
                        model.currentEnrollment.aggregatedSyncState() != SYNCED
                    _showStatusBar.value = model.currentEnrollment.status()
                    _state.value =
                        model.currentEnrollment.aggregatedSyncState()
                    _noEnrollmentSelected.postValue(false)
                    // This block runs on IO (by design, to keep the navigation-bar
                    // predicates off the main thread). Thread-safety rules per type:
                    //   - LiveData writes (`_noEnrollmentSelected`, `_dashboardModel`)
                    //     MUST use `postValue(...)`.
                    //   - MutableStateFlow writes (`_showFollowUpBar`, `_syncNeeded`,
                    //     `_showStatusBar`, `_state`, `_navigationBarUIState`) can use
                    //     `value = ...` from any thread.
                    loadNavigationBarItems()
                } else {
                    _noEnrollmentSelected.postValue(true)
                }
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                CoroutineTracker.decrement()
            }
        }
    }

    private fun loadNavigationBarItems() {
        // Phase 1: publish the cheap tabs (Details, Notes) immediately so the navigation
        // bar is not blank while Phase 2's predicates run. Selection is intentionally
        // NOT resolved here: items.first() in landscape would be NOTES (Details is
        // portrait-only, Analytics/Relationships are deferred), clobbering the pre-fix
        // default and the user's prior selection on refresh.
        _navigationBarUIState.value = _navigationBarUIState.value.copy(
            items = buildNavigationBarItems(
                includeAnalytics = false,
                includeRelationships = false,
            ),
        )

        // Phase 2: evaluate the expensive predicates (we are on IO) and republish the
        // final item list. Compose/Lifecycle collectors pick the update up on main.
        _navigationBarUIState.value = _navigationBarUIState.value.copy(
            items = buildNavigationBarItems(
                includeAnalytics = pageConfigurator.displayAnalytics(),
                includeRelationships = pageConfigurator.displayRelationships(),
            ),
        )
        // Resolve the selection against the FINAL item set so first-load picks the
        // pre-fix default (e.g. ANALYTICS in landscape) and refresh preserves the
        // user's prior selection (matches pre-fix single-phase behavior).
        if (navigationBarUIState.value.items.none { it.id == navigationBarUIState.value.selectedItem }) {
            onNavigationItemSelected(
                navigationBarUIState.value.items
                    .first()
                    .id,
            )
        }
    }

    private fun buildNavigationBarItems(
        includeAnalytics: Boolean,
        includeRelationships: Boolean,
    ): List<NavigationBarItem<TEIDashboardItems>> {
        val items = mutableListOf<NavigationBarItem<TEIDashboardItems>>()

        // TODO: inject OrientationProvider instead of calling the top-level isPortrait()
        // (tracked in openspec/changes/refactor-dashboard-viewmodel-orientation-injection).
        // This unblocks the deferred DashboardViewModelTest dispatcher/timing assertion.
        if (isPortrait()) {
            items.add(
                NavigationBarItem(
                    id = TEIDashboardItems.DETAILS,
                    icon = Icons.AutoMirrored.Outlined.Assignment,
                    selectedIcon = Icons.AutoMirrored.Filled.Assignment,
                    label = resourcesManager.getString(R.string.navigation_tei_data),
                ),
            )
        }

        if (includeAnalytics) {
            items.add(
                NavigationBarItem(
                    id = TEIDashboardItems.ANALYTICS,
                    icon = Icons.Outlined.BarChart,
                    selectedIcon = Icons.Filled.BarChart,
                    label = resourcesManager.getString(R.string.navigation_analytics),
                ),
            )
        }

        if (includeRelationships) {
            items.add(
                NavigationBarItem(
                    id = TEIDashboardItems.RELATIONSHIPS,
                    icon = Icons.Outlined.Hub,
                    selectedIcon = Icons.Filled.Hub,
                    label = resourcesManager.getString(R.string.navigation_relations),
                ),
            )
        }

        items.add(
            NavigationBarItem(
                id = TEIDashboardItems.NOTES,
                icon = Icons.AutoMirrored.Outlined.StickyNote2,
                selectedIcon = Icons.AutoMirrored.Filled.StickyNote2,
                label = resourcesManager.getString(R.string.navigation_notes),
            ),
        )

        return items
    }

    private fun fetchGrouping() {
        viewModelScope.launch(dispatcher.io()) {
            val result =
                async {
                    repository.getGrouping()
                }
            try {
                _groupByStage.postValue(result.await())
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun setGrouping(groupEvents: Boolean) {
        repository.setGrouping(groupEvents)
        _groupByStage.value = groupEvents
    }

    fun eventUid(): LiveData<String> = eventUid

    fun updateDashboard() {
        fetchDashboardModel()
    }

    fun updateEventUid(uid: String?) {
        if (eventUid.value != uid) {
            this.eventUid.value = uid
        }
    }

    fun onFollowUp() {
        if (dashboardModel.value is DashboardEnrollmentModel) {
            _showFollowUpBar.value =
                repository.setFollowUp((dashboardModel.value as DashboardEnrollmentModel).currentEnrollment.uid())
            _syncNeeded.value = true
            _state.value = State.TO_UPDATE
            analyticsHelper.setEvent(ACTIVE_FOLLOW_UP, _showFollowUpBar.value.toString(), FOLLOW_UP)
            updateDashboard()
        }
    }

    fun updateEnrollmentStatus(status: EnrollmentStatus) {
        viewModelScope.launch(dispatcher.io()) {
            if (dashboardModel.value is DashboardEnrollmentModel) {
                val result =
                    repository
                        .updateEnrollmentStatus(
                            (dashboardModel.value as DashboardEnrollmentModel).currentEnrollment.uid(),
                            status,
                        ).blockingFirst()

                if (result == StatusChangeResultCode.CHANGED) {
                    _syncNeeded.value = true
                    _state.value = State.TO_UPDATE
                    fetchDashboardModel()
                } else {
                    showStatusErrorMessages.postValue(result)
                }
            }
        }
    }

    fun deleteEnrollment(
        onSuccess: (Boolean?) -> Unit,
        onAuthorityError: () -> Unit,
    ) {
        viewModelScope.launch(dispatcher.io()) {
            val result =
                async {
                    dashboardModel.value.takeIf { it is DashboardEnrollmentModel }?.let {
                        repository
                            .deleteEnrollment((it as DashboardEnrollmentModel).currentEnrollment.uid())
                            .blockingGet()
                    }
                }
            try {
                val hasMoreEnrollments = result.await()
                onSuccess(hasMoreEnrollments)
            } catch (e: AuthorityException) {
                onAuthorityError()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun selectedEventUid(): LiveData<String> = selectedEventUid

    fun updateSelectedEventUid(uid: String?) {
        if (selectedEventUid.value != uid) {
            this.selectedEventUid.value = uid
        }
    }

    fun updateNoteCounter(numberOfNotes: Int) {
        _navigationBarUIState.value =
            _navigationBarUIState.value.copy(
                items =
                    _navigationBarUIState.value.items.map {
                        if (it.id == TEIDashboardItems.NOTES) {
                            it.copy(showBadge = numberOfNotes > 0)
                        } else {
                            it
                        }
                    },
            )
    }

    fun onNavigationItemSelected(itemId: TEIDashboardItems) {
        _navigationBarUIState.value = _navigationBarUIState.value.copy(selectedItem = itemId)
    }

    fun checkIfTeiCanBeTransferred(): Boolean = repository.teiCanBeTransferred()

    fun transferTei(
        newOrgUnitId: String,
        onCompletion: () -> Unit,
    ) {
        _isLoading.value = true
        viewModelScope.launch(dispatcher.io()) {
            try {
                repository.transferTei(newOrgUnitId)
                withContext(dispatcher.ui()) {
                    updateDashboard()
                    onCompletion()
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            } finally {
                withContext(dispatcher.ui()) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun updateRelationshipsTopBarIconState(state: RelationshipTopBarIconState) {
        _relationshipTopBarIconState.value = state
    }
}
