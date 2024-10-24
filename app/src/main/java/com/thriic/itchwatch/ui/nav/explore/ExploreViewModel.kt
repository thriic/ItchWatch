package com.thriic.itchwatch.ui.nav.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thriic.core.repository.GameRepository
import com.thriic.core.repository.SearchRepository
import com.thriic.itchwatch.ui.Navigator
import com.thriic.itchwatch.ui.utils.encodeUrl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val gameRepository: GameRepository,
    private val navigator: Navigator
): ViewModel() {

    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Init)
    val state: StateFlow<ExploreUiState>
        get() = _uiState

    private suspend fun update(uiState: ExploreUiState) = _uiState.emit(uiState)
    fun send(intent: ExploreIntent) = viewModelScope.launch { onHandle(intent) }
    private suspend fun onHandle(intent: ExploreIntent) {
        when (intent) {
            is ExploreIntent.OpenGame -> {
                gameRepository.getGameFull(intent.url) ?: gameRepository.getGameFull(intent.url, refresh = true)
                navigator.navigate("detail?url=${intent.url.encodeUrl()}&id=${intent.id}")
            }
            is ExploreIntent.Search -> {
                update(ExploreUiState.Loading)
                fetchKeywordSearch(intent.keyword)
                    .onSuccess { update(ExploreUiState.Ready(it)) }
                    .onFailure { update(ExploreUiState.Error(it.message)) }
            }
        }
    }
    private suspend fun fetchKeywordSearch(keyword:String, classification:String = "game") =
        searchRepository.fetchKeywordSearch(keyword, classification)
}

sealed interface ExploreIntent {
    data class Search(
        val keyword: String = "",
    ) : ExploreIntent
    data class OpenGame(val url: String,val id:String) : ExploreIntent
}