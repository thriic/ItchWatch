package com.thriic.itchwatch.ui.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import com.thriic.core.repository.GameRepository
import com.thriic.itchwatch.utils.decodeUrl
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


@HiltViewModel(assistedFactory = DetailViewModel.DetailViewModelFactory::class)
class DetailViewModel @AssistedInject constructor(
    private val repository: GameRepository,
    @Assisted val url: String
) : ViewModel() {
    @AssistedFactory
    interface DetailViewModelFactory {
        fun create(url: String): DetailViewModel
    }
    public override fun onCleared() {
        super.onCleared()
        Log.i("Detail ViewModel", "Detail Clear")
    }
   // private val url = //savedStateHandle.get<String>("url")?.decodeUrl() ?: throw Exception("url is null")

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Init)
    val state: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    fun sendMessage(message: String?) {
        viewModelScope.launch {
            _toastMessage.emit(message)
        }
    }

    private suspend fun update(uiState: DetailUiState) = _uiState.emit(uiState)
    fun send(intent: DetailIntent) = viewModelScope.launch { onHandle(intent) }
    private suspend fun onHandle(intent: DetailIntent) {

    }

    init {
        Log.i("Detail ViewModel","init url: $url")
        viewModelScope.launch {
            try {
                val game = repository.getGameFull(url)!! //?: repository.getGameFull(url, true)!!
                val starred = repository.existLocalGame(url)
                _uiState.value = DetailUiState.Ready(
                    game,
                    starred,
                    devLog = null
                )
            }catch (e:Exception){
                println(e)
                _uiState.value = DetailUiState.Error(e.message)
            }
        }
    }
}

sealed interface DetailIntent {
    data object Refresh : DetailIntent
    data class ClickTag(val tagDisplayName: String) : DetailIntent
    data class Starred(val starred: Boolean) : DetailIntent
}