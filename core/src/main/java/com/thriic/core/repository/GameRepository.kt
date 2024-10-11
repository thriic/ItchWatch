package com.thriic.core.repository

import android.util.Log
import com.thriic.core.local.GameLocalDataSource
import com.thriic.core.model.GameBasic
import com.thriic.core.model.Game
import com.thriic.core.model.toGameFull
import com.thriic.core.network.DevLogRemoteDataSource
import com.thriic.core.network.GameRemoteDataSource
import com.thriic.core.network.model.DevLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val gameRemoteDataSource: GameRemoteDataSource,
    private val devLogRemoteDataSource: DevLogRemoteDataSource,
    private val gameLocalDataSource: GameLocalDataSource
) {

    private var latestGames: MutableList<Game> = mutableListOf()
    private val failedList = mutableListOf<String>()

    //fetch a list of game basics
    fun getGameBasics(refresh: Boolean = false): Flow<Result<GameBasic>> =
        channelFlow {
            if (refresh) {
                flow {
                    for (game in latestGames) {
                        emit(game)
                    }
                }
                    .map { game ->
                        async(Dispatchers.IO) {
                            try {
                                val gameFull =
                                    gameRemoteDataSource.fetchGameData(game.url)
                                        .map { it.toGameFull() }
                                        .getOrThrow()
                                latestGames =
                                    latestGames.map { if (it.url == game.url) gameFull else it }
                                        .toMutableList() //update member variable
                                gameLocalDataSource.updateGames(gameFull)//update to database
                                return@async Result.success(gameFull.basic)
                            } catch (e: Exception) {
                                failedList.add(game.url)
                                this@channelFlow.send(Result.failure<GameBasic>(e))
                            }
                            return@async null
                        }
                    }
                    .buffer(5)
                    .collect { deferred ->
                        val gameBasic = deferred.await()
                        if (gameBasic != null)
                            this@channelFlow.send(gameBasic)
                    }
            } else {
                latestGames = gameLocalDataSource.getLocalGames().toMutableList()
                latestGames.forEach { gameFull ->
                    this@channelFlow.send(Result.success(gameFull.basic))
                }
            }
        }.buffer(5)

    fun addGames(urls: Set<String>): Flow<Result<GameBasic>> = channelFlow {
        flow {
            for (url in urls) {
                Log.i("GameRepository", url)
                emit(url)
            }
        }
            .map { url ->
                async(Dispatchers.IO) {
                    addGameAndEmit(url) { this@channelFlow.send(it) }
                }
            }
            .buffer(8)
            .collect { deferred ->
                val gameBasic = deferred.await()
                if (gameBasic != null)
                    this@channelFlow.send(gameBasic)
            }

    }

    fun addGame(url: String): Flow<Result<GameBasic>> = flow {
        val gameBasic = addGameAndEmit(url) { this@flow.emit(it) }
        if (gameBasic != null) emit(gameBasic)
    }

    private suspend fun addGameAndEmit(
        url: String,
        emit: suspend (Result<GameBasic>) -> Unit
    ): Result<GameBasic>? {
        if (latestGames.any { it.url == url }) {
            emit(Result.failure(Exception("Game already exists")))
            return null
        }
        try {
            val gameFull =
                gameRemoteDataSource.fetchGameData(url).map { it.toGameFull() }
                    .getOrThrow()
            latestGames.add(gameFull)//add to member variable
            gameLocalDataSource.insertGames(gameFull)//insert to database
            return Result.success(gameFull.basic)
        } catch (e: Exception) {
            failedList.add(url)
            emit(Result.failure(e))
        }
        return null
    }


    suspend fun deleteGame(url: String): Boolean {
        return if (latestGames.any { it.url == url }) {
            val game = latestGames.find { it.url == url }!!
            gameLocalDataSource.deleteGame(game)
            latestGames.remove(game)
            true
        } else false
    }

    private suspend fun getDevLog(url: String): DevLog? {
        return devLogRemoteDataSource.fetchDevLog(url).getOrNull()
    }


    suspend fun getGameFull(url: String, refresh: Boolean = false): Game? {
        return if (refresh) {
            //fetch and update to latestGames
            //wont insert to database
            gameRemoteDataSource.fetchGameData(url).getOrNull()?.toGameFull()
                ?.also { latestGames.add(it) }
        } else {
            latestGames.find { it.url == url }
        }
    }

    suspend fun existLocalGame(url: String): Boolean {
        return gameLocalDataSource.existGame(url)
    }
}