package com.thriic.itchwatch.ui.nav.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thriic.core.model.GameBasic
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
import javax.inject.Inject
import javax.inject.Singleton


@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: GameRepository,
    private val navigator: Navigator
) : ViewModel() {
    private val _items = MutableStateFlow<List<GameBasic>>(emptyList())
    val items: StateFlow<List<GameBasic>> = _items

    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState(
        SortType.Name, null,
        loading = false
    ))
    val state: StateFlow<LibraryUiState>
        get() = _uiState

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
                var index = 0
                update(_uiState.value.copy(loading = true))
                repository.getGameBasics(true).collect { result ->
                    index += 1
                    Log.i("Lib ViewModel", "refresh $result")
                    if (result.isSuccess) {
                        _items.value =
                            _items.value.map { if (it.url == result.getOrThrow().url) result.getOrThrow() else it }
                    }

                    if (size >= 5) update(_uiState.value.copy(progress = index / size.toFloat()))

                    if (index >= size) {
                        update(_uiState.value.copy(loading = false, progress = null))
                        sendMessage("refreshed")
                    }
                }
                update(_uiState.value.copy(loading = false, progress = null))
            }

            is LibraryIntent.AddGame -> {
                update(_uiState.value.copy(loading = true))
                repository.addGame(intent.url).collect { result ->
                    result
                        .onSuccess { _items.value += it }
                        .onFailure { it.message?.let { msg -> sendMessage(msg) } }
                    update(_uiState.value.copy(loading = false, progress = null))
                }
            }

            is LibraryIntent.ClickItem -> {
                Log.i("Lib ViewModel", "ClickItem: ${intent.url}")
                navigator.navigate("detail?url=${intent.url.encodeUrl()}&id=${intent.id}")
            }

            is LibraryIntent.Remove -> {
                val result = repository.deleteGame(intent.url)
                if (result) {
                    val mutableList = _items.value.toMutableList()
                    mutableList.removeIf { it.url == intent.url }
                    _items.value = mutableList
                    sendMessage("succeeded to remove")
                } else {
                    sendMessage("failed to remove")
                }
            }

            is LibraryIntent.AddGames -> {
                update(_uiState.value.copy(loading = true))
                if (intent.text.isNotBlank() && intent.text.isNotBlank()) {
                    val set = mutableSetOf<String>()
                    val matches =
                        Regex("(https://)?([^/]+)\\.itch\\.io/([A-Za-z0-9-]+)").findAll(intent.text)
                    matches.map { it.value }.toList().forEach {
                        set.add(it.cleanUrl())
                    }
                    Log.i("Lib ViewModel", "AddGames: $set")
                    if (set.isEmpty()) {
                        sendMessage("no valid url")
                        update(_uiState.value.copy(loading = false, progress = null))
                    } else {
                        val size = set.size
                        var index = 0
                        var successIndex = 0
                        update(_uiState.value.copy(loading = true))
                        sendMessage("try $size")
                        repository.addGames(urls = set).collect { result ->
                            index += 1
                            Log.i("Lib ViewModel", "add $result")
                            if (result.isSuccess) {
                                successIndex += 1
                                _items.value += result.getOrThrow()
                            }
                            if (size >= 5) update(_uiState.value.copy(progress = index / size.toFloat()))
                            if (index >= size) {
                                update(_uiState.value.copy(loading = false, progress = null))
                                sendMessage("refreshed $successIndex/$size")
                            }
                        }
                        update(_uiState.value.copy(loading = false, progress = null))
                    }
                } else {
                    sendMessage("empty text")
                    update(_uiState.value.copy(loading = false, progress = null))
                }
            }

            is LibraryIntent.Sort -> {
                update(_uiState.value.copy(sortType = intent.sortType))
                when (intent.sortType) {
                    SortType.Name -> {
                        _items.value = _items.value.sortedWith(compareBy { it.name })
                    }

                    SortType.Time -> {
                        //Priority: updatedTime > first devLog > publishedTime
                        _items.value = _items.value.sortedBy {
                            if (it.updatedTime == null ) {
                                if(!it.devLogs.isNullOrEmpty()) {
                                    it.devLogs!![0].pubDate
                                }else{
                                    it.publishedTime
                                }
                            }else{
                                it.updatedTime
                            }
                        }
                    }

                    SortType.TimeReverse -> {
                        _items.value = _items.value.sortedByDescending {
                            if (it.updatedTime == null ) {
                                if(!it.devLogs.isNullOrEmpty()) {
                                    it.devLogs!![0].pubDate
                                }else{
                                    it.publishedTime
                                }
                            }else{
                                it.updatedTime
                            }
                        }
                    }
                }
            }
        }
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
    data class AddGame(val url: String) : LibraryIntent
    data object Refresh : LibraryIntent
    data class ClickItem(val url: String, val id: Int) : LibraryIntent
    data class Remove(val url: String) : LibraryIntent
    data class AddGames(val text: String) : LibraryIntent
    data class Sort(val sortType: SortType) : LibraryIntent
}

enum class SortType {
    Name,
    Time,
    TimeReverse
}