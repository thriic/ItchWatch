package com.thriic.itchwatch.ui.nav.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thriic.core.local.GameLocalDataSource
import com.thriic.core.local.TagLocalDataSource
import com.thriic.core.model.LocalInfo
import com.thriic.core.network.SearchRemoteDataSource
import com.thriic.core.repository.SearchRepository
import com.thriic.itchwatch.utils.toLocalInfos
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val gameLocalDataSource: GameLocalDataSource,
    private val searchRemoteDataSource: SearchRemoteDataSource,
    private val tagLocalDataSource: TagLocalDataSource,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            SettingsState(0)
        )
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    fun sendMessage(message: String?) {
        viewModelScope.launch {
            _toastMessage.emit(message)
        }
    }

    private suspend fun update(block: SettingsState.() -> SettingsState) {
        val newState: SettingsState
        uiState.value.apply { newState = block() }
        _uiState.emit(newState)
    }

    fun send(intent: SettingsIntent) = viewModelScope.launch { onHandle(intent) }

    private suspend fun onHandle(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.Export -> {
                intent.callback(gameLocalDataSource.getAllLocalInfo())
            }

            is SettingsIntent.Import -> {
                val localInfos = intent.content.toLocalInfos().toTypedArray()
                gameLocalDataSource.insertLocalInfo(*localInfos)
                sendMessage("import successfully,${localInfos.size} in total")
            }

            SettingsIntent.UpdateSearchTags -> {
                val tags = searchRemoteDataSource.fetchAllTags().getOrThrow()
                val diff = tagLocalDataSource.updateSearchTagIfNew(*tags.toTypedArray())
                val size = tagLocalDataSource.count()
                sendMessage("updated $diff tags")
                update { copy(tagSize = size) }
            }
        }
    }
}

data class SettingsState(val tagSize:Int)

sealed interface SettingsIntent {
    data class Export(val callback: (List<LocalInfo>) -> Unit) : SettingsIntent
    data class Import(val content: String) : SettingsIntent
    data object UpdateSearchTags : SettingsIntent
}