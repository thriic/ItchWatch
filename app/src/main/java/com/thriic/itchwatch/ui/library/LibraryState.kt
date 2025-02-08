package com.thriic.itchwatch.ui.library

import com.thriic.core.model.Game
import com.thriic.core.model.LocalInfo
import com.thriic.core.model.FilterTag
import com.thriic.core.model.SortType


data class LibraryUiState(val sortTypes: Set<SortType>, val progress: Float? = null, val loading:Boolean,)

data class FilterState(val keyword:String, val filterTags: Set<FilterTag>)

