package com.eboof.firestickdashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eboof.firestickdashboard.data.DashboardRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val loading: Boolean = true,
    val state: com.eboof.firestickdashboard.data.DashboardState? = null,
    val activeBaseUrl: String? = null,
    val error: String? = null
)

class MainViewModel(
    private val repository: DashboardRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    init {
        startPolling()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            runCatching { repository.fetchState() }
                .onSuccess { (baseUrl, state) ->
                    _uiState.value = DashboardUiState(
                        loading = false,
                        state = state,
                        activeBaseUrl = baseUrl
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(loading = false, error = error.message ?: "Unknown error")
                }
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                refresh()
                delay(60_000)
            }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }
}

class MainViewModelFactory(
    private val repository: DashboardRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(repository) as T
}
