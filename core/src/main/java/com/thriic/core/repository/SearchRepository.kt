package com.thriic.core.repository

import com.thriic.core.network.SearchRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val searchRemoteDataSource: SearchRemoteDataSource,
){
    suspend fun fetchKeywordSearch(keyword:String, classification:String = "game") =
        searchRemoteDataSource.fetchKeywordSearch(keyword, classification)

}