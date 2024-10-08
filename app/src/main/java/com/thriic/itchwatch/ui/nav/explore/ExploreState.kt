package com.thriic.itchwatch.ui.nav.explore

import com.thriic.core.network.model.SearchApiModel

sealed interface ExploreUiState {
    data object Init : ExploreUiState
    data object Loading : ExploreUiState

    data class Error(
        val errorMessage: String? = null
    ) : ExploreUiState

    data class Ready(
        val searchApiModel: SearchApiModel
    ) : ExploreUiState
}