package com.thriic.core.repository

import android.util.Log
import com.thriic.core.local.GameLocalDataSource
import com.thriic.core.model.GameBasic
import com.thriic.core.model.Game
import com.thriic.core.model.toGameFull
import com.thriic.core.network.DevLogRemoteDataSource
import com.thriic.core.network.GameRemoteDataSource
import com.thriic.core.network.model.DevLog
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
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
                coroutineScope {
                    for (game in latestGames) {
                        //fetch one by one preventing "too many requests"

                        async { getGameAndEmit(game.url) { this@channelFlow.send(it) } }.await()
                    }
                }
            } else {
                latestGames = gameLocalDataSource.getLocalGames().toMutableList()
                latestGames.forEach { gameFull ->
                    this@channelFlow.send(Result.success(gameFull.basic))
                }
            }
        }

    fun addGames(urls: Set<String>): Flow<Result<GameBasic>> = channelFlow {
        coroutineScope {
            for (url in urls) {
                Log.i("GameRepository", url)
                async { addGameAndEmit(url) { this@channelFlow.send(it) } }.await()
            }
        }

    }


    fun addGame(url: String): Flow<Result<GameBasic>> = flow {
        addGameAndEmit(url){ this@flow.emit(it) }
    }

    suspend fun addGameAndEmit(url: String, emit: suspend (Result<GameBasic>) -> Unit){
        if (latestGames.any { it.url == url }) {
            emit(Result.failure(Exception("Game already exists")))
            return
        }
        try {
            val gameFull =
                gameRemoteDataSource.fetchGameData(url).map { it.toGameFull() }
                    .getOrThrow()
            latestGames.add(gameFull)//add to member variable
            gameLocalDataSource.insertGames(gameFull)//insert to database
            emit(Result.success(gameFull.basic))
        } catch (e: Exception) {
            failedList.add(url)
            emit(Result.failure(e))
        }
    }

    private suspend fun getGameAndEmit(url: String, emit: suspend (Result<GameBasic>) -> Unit) {
        try {
            val gameFull =
                gameRemoteDataSource.fetchGameData(url).map { it.toGameFull() }
                    .getOrThrow()
            latestGames = latestGames.map { if (it.url == url) gameFull else it }
                .toMutableList() //update member variable
            gameLocalDataSource.updateGames(gameFull)//update to database
            emit(Result.success(gameFull.basic))
        } catch (e: Exception) {
            failedList.add(url)
            emit(Result.failure(e))
        }
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