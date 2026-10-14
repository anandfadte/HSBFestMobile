package org.zenith26.fest.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.zenith26.fest.data.model.EventOut
import org.zenith26.fest.data.model.RegistrationIn
import org.zenith26.fest.data.repository.ZenithRepository

data class StudentUiState(
    val events: List<EventOut> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val selectedEventForDetail: EventOut? = null,
    val isLoadingDetail: Boolean = false,
    val isSubmittingRegistration: Boolean = false,
    val registrationAlert: Pair<Boolean, String>? = null // isSuccess to message
)

class StudentViewModel(
    private val repository: ZenithRepository = ZenithRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentUiState())
    val uiState: StateFlow<StudentUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        fetchEvents()
    }

    fun fetchEvents() {
        val category = _uiState.value.selectedCategory
        val search = _uiState.value.searchQuery.trim()

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.getEvents(
                category = if (category == "All") null else category,
                search = if (search.isEmpty()) null else search
            )
            result.onSuccess { events ->
                _uiState.update {
                    it.copy(
                        events = events,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.message ?: "Failed to connect to backend server. Make sure API is running."
                    )
                }
            }
        }
    }

    fun setCategory(category: String) {
        if (_uiState.value.selectedCategory == category) return
        _uiState.update { it.copy(selectedCategory = category) }
        fetchEvents()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(250) // 250ms debounce matching web app
            fetchEvents()
        }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "") }
        fetchEvents()
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(
                selectedCategory = "All",
                searchQuery = ""
            )
        }
        fetchEvents()
    }

    fun openEventDetail(eventId: Int) {
        _uiState.update { it.copy(isLoadingDetail = true, registrationAlert = null) }
        viewModelScope.launch {
            val result = repository.getEventDetails(eventId)
            result.onSuccess { event ->
                _uiState.update {
                    it.copy(
                        selectedEventForDetail = event,
                        isLoadingDetail = false
                    )
                }
            }.onFailure {
                // Fallback to local cache if network detail fails
                val cached = _uiState.value.events.firstOrNull { it.id == eventId }
                _uiState.update {
                    it.copy(
                        selectedEventForDetail = cached,
                        isLoadingDetail = false
                    )
                }
            }
        }
    }

    fun closeEventDetail() {
        _uiState.update {
            it.copy(
                selectedEventForDetail = null,
                registrationAlert = null,
                isSubmittingRegistration = false
            )
        }
    }

    fun submitRegistration(
        eventId: Int,
        studentName: String,
        rollNumber: String,
        email: String,
        phone: String?,
        onSuccess: () -> Unit
    ) {
        _uiState.update {
            it.copy(
                isSubmittingRegistration = true,
                registrationAlert = null
            )
        }

        viewModelScope.launch {
            val payload = RegistrationIn(
                studentName = studentName.trim(),
                rollNumber = rollNumber.trim(),
                email = email.trim(),
                phone = if (!phone.isNullOrBlank()) phone.trim() else null
            )

            val result = repository.registerForEvent(eventId, payload)

            result.onSuccess { regOut ->
                val eventTitle = _uiState.value.selectedEventForDetail?.title ?: "Event"
                _uiState.update {
                    it.copy(
                        isSubmittingRegistration = false,
                        registrationAlert = Pair(
                            true,
                            "🎉 Registration Confirmed! ${regOut.studentName} is registered for $eventTitle."
                        )
                    )
                }
                // Refresh events list to update seat count live on cards
                fetchEvents()
                // Re-fetch event detail to update available seats in dialog
                repository.getEventDetails(eventId).onSuccess { updatedEvent ->
                    _uiState.update { it.copy(selectedEventForDetail = updatedEvent) }
                }
                onSuccess()
            }.onFailure { err ->
                // Show exact server error message (e.g. "Event is full", "Already registered")
                val errMsg = err.message ?: "Registration failed. Please check your details."
                _uiState.update {
                    it.copy(
                        isSubmittingRegistration = false,
                        registrationAlert = Pair(false, errMsg)
                    )
                }
            }
        }
    }
}
