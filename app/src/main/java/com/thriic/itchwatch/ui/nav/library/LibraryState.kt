package com.thriic.itchwatch.ui.nav.library

import com.thriic.core.model.Game
import com.thriic.core.model.LocalInfo
import com.thriic.core.model.Tag


data class LibraryUiState(val sortTypes: Set<SortType>,val progress: Float? = null,val loading:Boolean,)

data class FilterState(val keyword:String, val tags: Set<Tag>)

data class DetailState(val game: Game?,val localInfo: LocalInfo?)