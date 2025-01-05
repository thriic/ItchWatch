package com.thriic.core.repository

import com.thriic.core.network.CollectionRemoteDataSource
import com.thriic.core.network.SearchRemoteDataSource
import com.thriic.core.network.model.CollectionApiModel
import com.thriic.core.network.model.GameCell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepository @Inject constructor(
    private val collectionRemoteDataSource: CollectionRemoteDataSource,
){
    //a collection url should be in this format:"https://itch.io/c/184xxxx(number id)/collection-name"
    suspend fun fetchCollection(url:String): Result<CollectionApiModel> {
        collectionRemoteDataSource.fetchCollection(url)
            .onSuccess { data ->
                if(data.gameCells.size < 20){
                    return Result.success(data)
                }
                //fetch last
                fetchLastPages(url)
                    .onSuccess { allGameCells ->
                        val fullCollection = data.copy(gameCells = data.gameCells+allGameCells)
                        return Result.success(fullCollection)
                    }
                    .onFailure { error ->
                        return Result.failure(Exception(error.message))
                    }
            }.onFailure { error ->
                return Result.failure(Exception(error.message))
            }
        return Result.failure(Exception("fetch collection returned empty"))
    }

    suspend fun fetchLastPages(url: String): Result<List<GameCell>> {
        val allGameCells = mutableListOf<GameCell>()
        var currentPage = 2

        return runCatching {
            withContext(Dispatchers.IO) {
                while (true) {
                    val pageResult = collectionRemoteDataSource.fetchCollectionJSON(url, page = currentPage)
                    if (pageResult.isFailure) {
                        throw pageResult.exceptionOrNull() ?: IOException("Unknown error")
                    }

                    val gameCells = pageResult.getOrThrow()
                    if (gameCells.isEmpty()) break

                    allGameCells.addAll(gameCells)
                    currentPage += 1
                }

                allGameCells
            }
        }
    }


}