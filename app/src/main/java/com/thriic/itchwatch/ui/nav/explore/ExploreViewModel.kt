package com.thriic.itchwatch.ui.nav.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thriic.core.local.UserPreferences
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

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val gameRepository: GameRepository,
    private val userPreferences: UserPreferences,
): ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState(
        sortType = SearchSortType.Popular, searchLoading = false, detailLading = false,
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

    init {
        //load user preferences
        viewModelScope.launch {
            userPreferences.searchSortTypeFlow.collect {
                update(_uiState.value.copy(sortType = it))
            }
        }
    }

    private suspend fun update(uiState: ExploreUiState) = _uiState.emit(uiState)
    fun send(intent: ExploreIntent) = viewModelScope.launch { onHandle(intent) }
    private suspend fun onHandle(intent: ExploreIntent) {
        when (intent) {
            is ExploreIntent.ClickItem -> {
                update(_uiState.value.copy(detailLading = false))
                val game = gameRepository.getGameFull(intent.url)
                val localInfo = if(gameRepository.existLocalGame(intent.url)) gameRepository.getLocalInfo(intent.url) else null
                _detailState.emit(DetailState(game, localInfo))
                intent.callback()
                update(_uiState.value.copy(detailLading = true))
            }
            is ExploreIntent.SearchByKeyword -> {
                update(_uiState.value.copy(searchLoading = true))
                fetchKeywordSearch(intent.keyword)
                    .onSuccess { update(_uiState.value.copy(searchApiModel = it)) }
                    .onFailure { sendMessage(it.message) }
                update(_uiState.value.copy(searchLoading = false))
            }
            is ExploreIntent.SearchByTag -> {
                if(!_uiState.value.searchLoading) {
                    update(_uiState.value.copy(searchLoading = true))
                    //when change search sort type
                    if(intent.sortType!=null && state.value.sortType != intent.sortType) {
                        update(_uiState.value.copy(sortType = intent.sortType))
                        userPreferences.saveSearchSortType(intent.sortType)//save to preferences
                    }
                    searchRepository.fetchTagSearch(
                        tags = intent.tags,
                        sortType = state.value.sortType
                    )
                        .onSuccess { update(_uiState.value.copy(searchApiModel = it)) }
                        .onFailure { sendMessage(it.message) }
                    update(_uiState.value.copy(searchLoading = false))
                }
            }

            ExploreIntent.AllTags -> {
                if(_uiState.value.allTags.isEmpty()){
                    update(_uiState.value.copy(allTags = searchRepository.fetchAllTags()))
                }
            }

            is ExploreIntent.Star -> {
                try {
                    if(gameRepository.existLocalGame(intent.url)){
                        val localInfo = gameRepository.getLocalInfo(intent.url)
                        val updatedLocalInfo = gameRepository.updateLocalInfo(url = intent.url, starred = !localInfo.starred)
                        if(_detailState.value.game?.url == intent.url) _detailState.value = _detailState.value.copy(localInfo = updatedLocalInfo)
                        sendMessage(if(localInfo.starred) "unstarred" else "starred")
                    }else{
                        gameRepository.addGameByUrl(intent.url).collect { result ->
                            result
                                .onSuccess {
                                    val localInfo = gameRepository.getLocalInfo(intent.url)
                                    val updatedLocalInfo = gameRepository.updateLocalInfo(url = intent.url, starred = !localInfo.starred)
                                    if(_detailState.value.game?.url == intent.url) _detailState.value = _detailState.value.copy(localInfo = updatedLocalInfo)
                                    sendMessage(if(localInfo.starred) "unstarred" else "starred")
                                }
                                .onFailure { it.message?.let { msg -> sendMessage(msg) } }
                        }
                    }
                }catch (e:Exception){
                    sendMessage("err:"+e.message)
                }
            }

            is ExploreIntent.AddLocal -> {
                try {
                    if(gameRepository.existLocalGame(intent.url)){
                        sendMessage("has already in lib")
                    }else{
                        gameRepository.addGameByUrl(intent.url).collect { result ->
                            result
                                .onSuccess {
                                    sendMessage("added successfully")
                                }
                                .onFailure { it.message?.let { msg -> sendMessage(msg) } }
                        }
                    }
                }catch (e:Exception){
                    sendMessage("err:"+e.message)
                }
            }

        }
    }

    private suspend fun fetchKeywordSearch(keyword:String, classification:String = "game") =
        searchRepository.fetchKeywordSearch(keyword, classification)
}

sealed interface ExploreIntent {
    data class SearchByKeyword(val keyword: String = "") : ExploreIntent
    data class SearchByTag(val tags:List<SearchTag>, val sortType: SearchSortType? = null) : ExploreIntent
    data object AllTags : ExploreIntent
    data class ClickItem(val url: String, val callback: ()->Unit) : ExploreIntent
    data class Star(val url: String) : ExploreIntent
    data class AddLocal(val url: String): ExploreIntent
}