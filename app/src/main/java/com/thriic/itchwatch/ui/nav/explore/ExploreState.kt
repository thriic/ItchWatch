package com.thriic.itchwatch.ui.nav.explore

import com.thriic.core.model.SearchSortType
import com.thriic.core.model.SearchTag
import com.thriic.core.network.model.SearchApiModel
import com.thriic.core.network.model.SearchResult

data class ExploreUiState(
    val sortType: SearchSortType = SearchSortType.Popular,
    val searchLoading: Boolean,
    val detailLading: Boolean,
    val searchResults: List<SearchResult>? = null,
    val canLoadMore: Boolean = true,
    val allTags: List<SearchTag>
)