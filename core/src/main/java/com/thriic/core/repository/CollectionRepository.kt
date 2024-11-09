package com.thriic.core.repository

import com.thriic.core.network.CollectionRemoteDataSource
import com.thriic.core.network.SearchRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepository @Inject constructor(
    private val collectionRemoteDataSource: CollectionRemoteDataSource,
){
    //a collection url should be in this format:"https://itch.io/c/184xxxx(number id)/collection-name"
    suspend fun fetchCollection(url:String) =
        collectionRemoteDataSource.fetchCollection(url)

}