package com.thriic.core.repository

import com.thriic.core.local.TagLocalDataSource
import com.thriic.core.model.SearchSortType
import com.thriic.core.model.SearchTag
import com.thriic.core.network.SearchRemoteDataSource
import com.thriic.core.network.model.SearchApiModel
import com.thriic.core.network.model.SearchResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val searchRemoteDataSource: SearchRemoteDataSource,
    private val tagLocalDataSource: TagLocalDataSource,
){
    suspend fun fetchKeywordSearch(keyword:String, classification:String = "game") =
        searchRemoteDataSource.fetchKeywordSearch(keyword, classification)

    suspend fun fetchTagSearchPage(tags:List<SearchTag>, sortType: SearchSortType = SearchSortType.Popular): Result<SearchApiModel> {
        return searchRemoteDataSource.fetchTagSearch(tags, sortType)
    }
    suspend fun fetchTagSearchJSON(tags:List<SearchTag>, sortType: SearchSortType = SearchSortType.Popular, page:Int): Result<List<SearchResult>> {
        return searchRemoteDataSource.fetchTagSearchJSON(tags, sortType, page)
    }

    suspend fun fetchAllTags(): List<SearchTag> {
        //if empty fetch from itch page
        return if(tagLocalDataSource.empty()) {
            val tags = searchRemoteDataSource.fetchAllTags().getOrThrow()
            tagLocalDataSource.updateSearchTagIfNew(*tags.toTypedArray())
            tags
        }else{
            tagLocalDataSource.getAllSearchTag()
        }
    }


}