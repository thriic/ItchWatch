package com.thriic.itchwatch.ui.nav.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thriic.core.model.SearchSortType
import com.thriic.core.model.SearchTag
import com.thriic.core.repository.GameRepository
import com.thriic.core.repository.SearchRepository
import com.thriic.itchwatch.ui.detail.DetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.tanh

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val gameRepository: GameRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState(
        sortType = SearchSortType.Popular, loading = false,
        allTags = listOf()))
    val state: StateFlow<ExploreUiState>
        get() = _uiState

    private val _detailState = MutableStateFlow(DetailState(null,null))
    val detailState: StateFlow<DetailState>
        get() = _detailState

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    fun sendMessage(message: String?) {
        viewModelScope.launch {
            _toastMessage.emit(message)
        }
    }
    private suspend fun update(uiState: ExploreUiState) = _uiState.emit(uiState)
    fun send(intent: ExploreIntent) = viewModelScope.launch { onHandle(intent) }
    private suspend fun onHandle(intent: ExploreIntent) {
        when (intent) {
            is ExploreIntent.ClickItem -> {
                val game = gameRepository.getGameFull(intent.url)
                val localInfo = if(gameRepository.existLocalGame(intent.url)) gameRepository.getLocalInfo(intent.url) else null
                _detailState.emit(DetailState(game, localInfo))
                intent.callback()
            }
            is ExploreIntent.SearchByKeyword -> {
                update(_uiState.value.copy(loading = true))
                fetchKeywordSearch(intent.keyword)
                    .onSuccess { update(_uiState.value.copy(searchApiModel = it)) }
                    .onFailure { sendMessage(it.message) }
                update(_uiState.value.copy(loading = false))
            }
            is ExploreIntent.SearchByTag -> {
                update(_uiState.value.copy(loading = true))
                searchRepository.fetchTagSearch(tags = intent.tags, sortType = state.value.sortType)
                    .onSuccess { update(_uiState.value.copy(searchApiModel = it)) }
                    .onFailure { sendMessage(it.message) }
                update(_uiState.value.copy(loading = false))
            }

            ExploreIntent.AllTags -> {
                if(_uiState.value.allTags.isEmpty()){
                    update(_uiState.value.copy(allTags = searchRepository.fetchAllTags()))
                }
            }

            is ExploreIntent.Star -> {
                addLocalGame(intent.url)
                val localInfo = gameRepository.getLocalInfo(intent.url)
                val updatedLocalInfo = gameRepository.updateLocalInfo(url = intent.url, starred = !localInfo.starred)
                if(_detailState.value.game?.url == intent.url) _detailState.value = _detailState.value.copy(localInfo = updatedLocalInfo)
                sendMessage(if(localInfo.starred) "unstarred" else "starred")
            }

            is ExploreIntent.AddLocal -> {
                val result = addLocalGame(intent.url)
                if(result) sendMessage("added") else sendMessage("has already in lib")
            }
        }
    }

    private suspend fun addLocalGame(url:String):Boolean{
        return if(gameRepository.existLocalGame(url)){
            false
        }else{
            gameRepository.addGameByUrl(url)
            true
        }
    }

    private suspend fun fetchKeywordSearch(keyword:String, classification:String = "game") =
        searchRepository.fetchKeywordSearch(keyword, classification)
}

sealed interface ExploreIntent {
    data class SearchByKeyword(val keyword: String = "") : ExploreIntent
    data class SearchByTag(val tags:List<SearchTag>) : ExploreIntent
    data object AllTags : ExploreIntent
    data class ClickItem(val url: String, val callback: ()->Unit) : ExploreIntent
    data class Star(val url: String) : ExploreIntent
    data class AddLocal(val url: String): ExploreIntent
}