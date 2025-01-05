package com.thriic.itchwatch.ui.nav.imports

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thriic.core.model.GameBasic
import com.thriic.core.model.Tag
import com.thriic.core.repository.CollectionRepository
import com.thriic.core.repository.GameRepository
import com.thriic.itchwatch.ui.Navigator
import com.thriic.itchwatch.ui.nav.library.SortType
import com.thriic.itchwatch.ui.utils.cleanUrl
import com.thriic.itchwatch.ui.utils.encodeUrl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ImportViewModel @Inject constructor(
    private val repository: GameRepository,
    private val collectionRepository: CollectionRepository,
    private val navigator: Navigator
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ImportState(
        null,
        loading = false,
        progressText = ""
    )
    )
    val state: StateFlow<ImportState>
        get() = _uiState

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    fun sendMessage(message: String?) {
        viewModelScope.launch {
            _toastMessage.emit(message)
        }
    }

    suspend fun clearProgress(){
        update(_uiState.value.copy(loading = false, progress = null, progressText = ""))
    }

    private suspend fun update(uiState: ImportState) = _uiState.emit(uiState)

    fun send(intent: ImportIntent) = viewModelScope.launch { onHandle(intent) }
    private suspend fun onHandle(intent: ImportIntent) {
        Log.i("Lib ViewModel", "onHandle: $intent")
        when (intent) {

            is ImportIntent.AddGame -> {
                update(_uiState.value.copy(loading = true))
                repository.addGame(intent.url).collect { result ->
                    result
                        .onSuccess {
                            sendMessage("added successfully")
                            navigator.navigate("detail?url=${intent.url.encodeUrl()}&id=0")
                        }
                        .onFailure { it.message?.let { msg -> sendMessage(msg) } }
                    update(_uiState.value.copy(loading = false, progress = null, progressText = ""))
                }
            }

            is ImportIntent.AddGames -> {
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
                        update(_uiState.value.copy(loading = true, progressText = "trying $size links from the file"))
                        sendMessage("try $size")
                        repository.addGames(urls = set).collect { result ->
                            index += 1
                            Log.i("Lib ViewModel", "add $result")
                            if (result.isSuccess) {
                                successIndex += 1
                            }
                            if (size >= 5) update(_uiState.value.copy(progress = index / size.toFloat(), progressText = "fetching..."))
                            if (index >= size) {
                                clearProgress()
                                sendMessage("added $successIndex/$size")
                            }
                        }
                    }
                } else {
                    sendMessage("empty text")
                    update(_uiState.value.copy(loading = false, progress = null))
                }
            }

            ImportIntent.Finish -> {
                navigator.popBackStack()
            }

            is ImportIntent.FetchCollection -> {
                update(_uiState.value.copy(loading = true, progressText = "fetching collection"))
                collectionRepository.fetchCollection(intent.url)
                    .onSuccess { collection ->
                        val size = collection.gameCells.size
                        var index = 0
                        var successIndex = 0
                        update(_uiState.value.copy(loading = true, progressText = "fetching $size games from ${collection.title}"))
                        repository.addGames(collection.gameCells).collect { result ->
                            index += 1
                            Log.i("Lib ViewModel", "add $result")
                            if (result.isSuccess) {
                                successIndex += 1
                            }
                            if (size >= 5) update(_uiState.value.copy(progress = index / size.toFloat(), progressText = "fetching ${collection.gameCells[index-1].name}"))
                            if (index >= size) {
                                clearProgress()
                                sendMessage("added $successIndex/$size")
                            }
                        }
                    }
                    .onFailure {
                        clearProgress()
                    }
            }
        }
    }
}

data class ImportState(val progress: Float? = null, val loading:Boolean, val progressText:String)

sealed interface ImportIntent {
    data class AddGame(val url: String) : ImportIntent
    data class AddGames(val text: String) : ImportIntent
    data class FetchCollection(val url: String) : ImportIntent
    data object Finish : ImportIntent
}