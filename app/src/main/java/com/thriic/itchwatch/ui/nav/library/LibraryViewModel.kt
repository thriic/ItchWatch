package com.thriic.itchwatch.ui.nav.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thriic.core.model.GameBasic
import com.thriic.core.model.Tag
import com.thriic.core.repository.GameRepository
import com.thriic.itchwatch.ui.Navigator
import com.thriic.itchwatch.ui.utils.cleanUrl
import com.thriic.itchwatch.ui.utils.encodeUrl
import com.thriic.itchwatch.ui.utils.isGamePageUrl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton


@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: GameRepository,
    private val navigator: Navigator
) : ViewModel() {
    private val _items = MutableStateFlow<List<GameBasic>>(emptyList())
    val items: StateFlow<List<GameBasic>> = _items

    private val _uiState = MutableStateFlow(LibraryUiState(
        setOf(SortType.Name), null,
        loading = false
    ))
    val state: StateFlow<LibraryUiState>
        get() = _uiState

    private val _filterState = MutableStateFlow(FilterState("", mutableSetOf()))
    val filterState: StateFlow<FilterState>
        get() = _filterState

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    fun sendMessage(message: String?) {
        viewModelScope.launch {
            _toastMessage.emit(message)
        }
    }

    private suspend fun update(uiState: LibraryUiState) = _uiState.emit(uiState)
    fun send(intent: LibraryIntent) = viewModelScope.launch { onHandle(intent) }
    private suspend fun onHandle(intent: LibraryIntent) {
        Log.i("Lib ViewModel", "onHandle: $intent")
        when (intent) {
            LibraryIntent.Refresh -> {

                val size = _items.value.size
                var successIndex = 0
                var index = 0
                update(_uiState.value.copy(loading = true))
                repository.getGameBasics(true).collect { result ->
                    index += 1
                    Log.i("Lib ViewModel", "refresh $result")
                    if (result.isSuccess) {
                        successIndex += 1
                        _items.value =
                            _items.value.map { if (it.url == result.getOrThrow().url) result.getOrThrow() else it }
                    }

                    if (size >= 5) update(_uiState.value.copy(progress = index / size.toFloat()))

                    if (index >= size) {
                        update(_uiState.value.copy(loading = false, progress = null))
                        sendMessage("refreshed $successIndex/$size")
                    }
                }
                update(_uiState.value.copy(loading = false, progress = null))
                sort()
            }

//            is LibraryIntent.AddGame -> {
//                update(_uiState.value.copy(loading = true))
//                repository.addGame(intent.url).collect { result ->
//                    result
//                        .onSuccess { _items.value += it }
//                        .onFailure { it.message?.let { msg -> sendMessage(msg) } }
//                    update(_uiState.value.copy(loading = false, progress = null))
//                }
//            }

            is LibraryIntent.ClickItem -> {
                Log.i("Lib ViewModel", "ClickItem: ${intent.url}")
                navigator.navigate("detail?url=${intent.url.encodeUrl()}&id=${intent.id}")
            }


//            is LibraryIntent.AddGames -> {
//                update(_uiState.value.copy(loading = true))
//                if (intent.text.isNotBlank() && intent.text.isNotBlank()) {
//                    val set = mutableSetOf<String>()
//                    val matches =
//                        Regex("(https://)?([^/]+)\\.itch\\.io/([A-Za-z0-9-]+)").findAll(intent.text)
//                    matches.map { it.value }.toList().forEach {
//                        set.add(it.cleanUrl())
//                    }
//                    Log.i("Lib ViewModel", "AddGames: $set")
//                    if (set.isEmpty()) {
//                        sendMessage("no valid url")
//                        update(_uiState.value.copy(loading = false, progress = null))
//                    } else {
//                        val size = set.size
//                        var index = 0
//                        var successIndex = 0
//                        update(_uiState.value.copy(loading = true))
//                        sendMessage("try $size")
//                        repository.addGames(urls = set).collect { result ->
//                            index += 1
//                            Log.i("Lib ViewModel", "add $result")
//                            if (result.isSuccess) {
//                                successIndex += 1
//                                _items.value += result.getOrThrow()
//                            }
//                            if (size >= 5) update(_uiState.value.copy(progress = index / size.toFloat()))
//                            if (index >= size) {
//                                update(_uiState.value.copy(loading = false, progress = null))
//                                sendMessage("refreshed $successIndex/$size")
//                            }
//                        }
//                        update(_uiState.value.copy(loading = false, progress = null))
//                    }
//                } else {
//                    sendMessage("empty text")
//                    update(_uiState.value.copy(loading = false, progress = null))
//                }
//            }

            is LibraryIntent.Sort -> {
                val newSet = _uiState.value.sortTypes.toMutableSet()
                when {
                    intent.sortType == SortType.TimeReverse -> {
                        newSet.remove(SortType.Name)
                        newSet.add(SortType.TimeReverse)
                    }
                    intent.sortType == SortType.Name -> {
                        newSet.remove(SortType.TimeReverse)
                        newSet.add(SortType.Name)
                    }
                    _uiState.value.sortTypes.contains(intent.sortType) -> {
                        newSet.remove(intent.sortType)
                    }
                    else -> {
                        newSet.add(intent.sortType)
                    }
                }
                update(_uiState.value.copy(sortTypes = newSet))
                sort()
            }

            is LibraryIntent.UpdateFilter -> {
                if (intent.keyword != null){
                    _filterState.emit(_filterState.value.copy(keyword = intent.keyword))
                }
                if (intent.tags != null){
                    _filterState.emit(_filterState.value.copy(tags = intent.tags))
                }
            }

            LibraryIntent.SyncRepository -> {
                if(repository.size() > _items.value.size) {
                    Log.i("Lib ViewModel", "sync")
                    _items.value = repository.syncGameBasic()
                    sort()
                }
            }

            is LibraryIntent.Remove -> {
                val result = repository.deleteGame(intent.url)
                if (result) {
                    val mutableList = _items.value.toMutableList()
                    mutableList.removeIf { it.url == intent.url }
                    _items.value = mutableList
                    sendMessage("removed")
                } else {
                    sendMessage("failed to remove")
                }
            }

            is LibraryIntent.Mark -> {
                val gameBasic = _items.value.firstOrNull { it.url == intent.url }
                if(gameBasic == null) throw Exception("cannot find the specified game")
                val updatedLocalInfo = repository.updateLocalInfo(url = intent.url, lastPlayedTime = LocalDateTime.now(), lastPlayedVersion = gameBasic.versionOrFileName)
                _items.value =
                    _items.value.map { if (it.url == intent.url) gameBasic.copy(localInfo = updatedLocalInfo) else it }
                sendMessage("marked")
            }

            is LibraryIntent.Star -> {
                val gameBasic = _items.value.firstOrNull { it.url == intent.url }
                if(gameBasic == null) throw Exception("cannot find the specified game")
                val updatedLocalInfo = repository.updateLocalInfo(url = intent.url, starred = !gameBasic.localInfo.starred)
                _items.value =
                    _items.value.map { if (it.url == intent.url) gameBasic.copy(localInfo = updatedLocalInfo) else it }
                sendMessage(if(gameBasic.localInfo.starred) "unstarred" else "starred")
            }
        }
    }

    private fun sort(sortTypes: Set<SortType> = _uiState.value.sortTypes){
        //Log.i("Lib ViewModel", sortTypes.toString())
        fun getEffectiveTime(game: GameBasic): LocalDateTime {
            return when {
                game.updatedTime != null -> game.updatedTime!!
                !game.devLogs.isNullOrEmpty() -> game.devLogs!![0].pubDate
                else -> game.publishedTime ?: LocalDateTime.MIN // 使用最早的时间作为后备
            }
        }
        _items.value = _items.value.sortedWith { game1, game2 ->
            var result = 0

            // 1. Starred 优先级
            if (SortType.Starred in sortTypes) {
                result = game2.localInfo.starred.compareTo(game1.localInfo.starred) // true > false
            }

            // 2. Updated 优先级
            if (result == 0 && SortType.Updated in sortTypes) {
                result = game2.updated.compareTo(game1.updated) // true > false
            }

            // 3. TimeReverse 排序
            if (result == 0 && SortType.TimeReverse in sortTypes) {
                val time1 = getEffectiveTime(game1)
                val time2 = getEffectiveTime(game2)
                result = time2.compareTo(time1) // 降序排序
            }

            // 4. Name 排序（在最后）
            if (result == 0 && SortType.Name in sortTypes) {
                result = game1.name.compareTo(game2.name)
            }

            result
        }

//            SortType.TimeReverse -> {
//                _items.value = _items.value.sortedByDescending {
//                    if (it.updatedTime == null ) {
//                        if(!it.devLogs.isNullOrEmpty()) {
//                            it.devLogs!![0].pubDate
//                        }else{
//                            it.publishedTime
//                        }
//                    }else{
//                        it.updatedTime
//                    }
//                }
//            }

    }

    init {
        viewModelScope.launch {
            repository.getGameBasics().collect { result ->
                if (result.isSuccess)
                    _items.value += result.getOrThrow()
            }
        }
    }
}

sealed interface LibraryIntent {
    //data class AddGame(val url: String) : LibraryIntent
    data object Refresh : LibraryIntent
    data class ClickItem(val url: String, val id: String) : LibraryIntent
    data class Remove(val url: String) : LibraryIntent
    data class Star(val url: String) : LibraryIntent
    data class Mark(val url: String) : LibraryIntent
    //data class AddGames(val text: String) : LibraryIntent
    data class Sort(val sortType: SortType) : LibraryIntent
    data class UpdateFilter(val keyword:String?, val tags: Set<Tag>?): LibraryIntent
    data object SyncRepository : LibraryIntent
}

enum class SortType {
    Name,
    Time,
    TimeReverse,
    Starred,
    Updated
}