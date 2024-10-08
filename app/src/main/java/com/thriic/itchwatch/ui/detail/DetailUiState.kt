package com.thriic.itchwatch.ui.detail

import com.thriic.core.model.Game
import com.thriic.core.network.model.DevLog

sealed interface DetailUiState {
    data object Init : DetailUiState

    data class Error(
        val errorMessage: String? = null
    ) : DetailUiState

    data class Ready(
        val game:Game,
        val starred: Boolean = false,
        val devLog: DevLog?,
    ) : DetailUiState
}