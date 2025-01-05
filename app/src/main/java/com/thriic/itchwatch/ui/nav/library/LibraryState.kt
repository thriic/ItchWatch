package com.thriic.itchwatch.ui.nav.library

import com.thriic.core.model.Tag


data class LibraryUiState(val sortType: SortType,val progress: Float? = null,val loading:Boolean,)

data class FilterState(val keyword:String, val tags: Set<Tag>)