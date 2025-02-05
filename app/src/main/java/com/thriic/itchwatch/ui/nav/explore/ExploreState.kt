package com.thriic.itchwatch.ui.nav.explore

import com.thriic.core.model.SearchSortType
import com.thriic.core.model.SearchTag
import com.thriic.core.network.model.SearchApiModel

data class ExploreUiState(
    val sortType: SearchSortType = SearchSortType.Popular,
    val searchLoading: Boolean,
    val detailLading: Boolean,
    val searchApiModel: SearchApiModel? = null,
    val allTags: List<SearchTag>
)