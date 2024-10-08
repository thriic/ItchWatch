package com.thriic.itchwatch.ui.nav.library

import com.thriic.core.network.model.SearchApiModel

data class LibraryUiState(val sortType: SortType,val progress: Float? = null,val loading:Boolean,)
