package org.zenith26.fest.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.zenith26.fest.data.model.EventCreate
import org.zenith26.fest.data.model.EventOut
import org.zenith26.fest.data.model.RegistrationItemOut
import org.zenith26.fest.data.model.StatsOut
import org.zenith26.fest.data.repository.ZenithRepository

data class AdminUiState(
    val stats: StatsOut? = null,
    val events: List<EventOut> = emptyList(),
    val registrations: List<RegistrationItemOut> = emptyList(),
    val selectedTab: Int = 0, // 0 = Utilization Table, 1 = Student Registrations
    val tableSearchQuery: String = "",
    val selectedFilterEventId: Int? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAddEventDialogOpen: Boolean = false,
    val isAddingEvent: Boolean = false,
    val addEventAlert: Pair<Boolean, String>? = null
)

class AdminViewModel(
    private val repository: ZenithRepository = ZenithRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        refreshAllData()
    }

    fun refreshAllData() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            fetchStatsInternal()
            fetchEventsInternal()
            fetchRegistrationsInternal(_uiState.value.selectedFilterEventId)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun fetchStatsInternal() {
        repository.getStats().onSuccess { stats ->
            _uiState.update { it.copy(stats = stats) }
        }.onFailure { err ->
            _uiState.update {
                it.copy(errorMessage = err.message ?: "Failed to fetch stats")
            }
        }
    }

    private suspend fun fetchEventsInternal() {
        repository.getEvents().onSuccess { events ->
            _uiState.update { it.copy(events = events) }
        }.onFailure { err ->
            _uiState.update {
                it.copy(errorMessage = err.message ?: "Failed to fetch events")
            }
        }
    }

    private suspend fun fetchRegistrationsInternal(eventId: Int?) {
        repository.getRegistrations(eventId).onSuccess { list ->
            _uiState.update { it.copy(registrations = list) }
        }.onFailure { err ->
            _uiState.update {
                it.copy(errorMessage = err.message ?: "Failed to fetch registrations")
            }
        }
    }

    fun setTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == 1) {
            viewModelScope.launch {
                fetchRegistrationsInternal(_uiState.value.selectedFilterEventId)
            }
        }
    }

    fun setTableSearch(query: String) {
        _uiState.update { it.copy(tableSearchQuery = query) }
    }

    fun setFilterEventId(eventId: Int?) {
        _uiState.update { it.copy(selectedFilterEventId = eventId) }
        viewModelScope.launch {
            fetchRegistrationsInternal(eventId)
        }
    }

    fun cancelRegistration(registrationId: Int) {
        viewModelScope.launch {
            repository.cancelRegistration(registrationId).onSuccess {
                refreshAllData()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(errorMessage = err.message ?: "Could not cancel registration")
                }
            }
        }
    }

    fun openAddEventDialog() {
        _uiState.update {
            it.copy(isAddEventDialogOpen = true, addEventAlert = null)
        }
    }

    fun closeAddEventDialog() {
        _uiState.update {
            it.copy(isAddEventDialogOpen = false, addEventAlert = null, isAddingEvent = false)
        }
    }

    fun createEvent(payload: EventCreate) {
        _uiState.update { it.copy(isAddingEvent = true, addEventAlert = null) }
        viewModelScope.launch {
            repository.createEvent(payload).onSuccess { created ->
                _uiState.update {
                    it.copy(
                        isAddingEvent = false,
                        addEventAlert = Pair(true, "Event \"${created.title}\" added successfully!")
                    )
                }
                refreshAllData()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isAddingEvent = false,
                        addEventAlert = Pair(false, err.message ?: "Failed to create event")
                    )
                }
            }
        }
    }
}
