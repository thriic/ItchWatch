package com.thriic.core.repository

import android.util.Log
import com.thriic.core.local.GameLocalDataSource
import com.thriic.core.model.GameBasic
import com.thriic.core.model.Game
import com.thriic.core.model.LocalInfo
import com.thriic.core.model.toGameFull
import com.thriic.core.network.DevLogRemoteDataSource
import com.thriic.core.network.GameRemoteDataSource
import com.thriic.core.network.model.DevLog
import com.thriic.core.network.model.GameCell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val gameRemoteDataSource: GameRemoteDataSource,
    private val devLogRemoteDataSource: DevLogRemoteDataSource,
    private val gameLocalDataSource: GameLocalDataSource
) {

    private var latestGames: MutableList<Game> = mutableListOf()
    private var tempGames: MutableList<Game> = mutableListOf()
    private val failedList = mutableListOf<String>()

    //fetch a list of game basics
    //when refresh is true, fetch from remote source and update in database
    //when false, fetch from local source
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
                                val basicWithLocal = gameFull.toBasic(getLocalInfo(gameFull.url))
                                return@async Result.success(basicWithLocal)
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
                    this@channelFlow.send(Result.success(gameFull.toBasic(getLocalInfo(gameFull.url))))
                }
            }
        }.buffer(5)

    //import games
    fun addGames(urls: Set<String>, withLocalInfo: Boolean = false): Flow<Result<GameBasic>> = channelFlow {
        flow {
            for (url in urls) {
                Log.i("GameRepository", url)
                emit(url)
            }
        }
            .map { url ->
                async(Dispatchers.IO) {
                    addGameAndEmit(url,withLocalInfo = withLocalInfo) { this@channelFlow.send(it) }
                }
            }
            .buffer(8)
            .collect { deferred ->
                val gameBasic = deferred.await()
                if (gameBasic != null)
                    this@channelFlow.send(gameBasic)
            }
    }

    //import game from collection,
    //with blurb
    fun addGames(gameCells: List<GameCell>): Flow<Result<GameBasic>> = channelFlow {
        flow {
            for (gameCell in gameCells) {
                Log.i("GameRepository", gameCell.url)
                emit(gameCell)
            }
        }
            .map { gameCell ->
                async(Dispatchers.IO) {
                    addGameAndEmit(gameCell.url, gameCell.blurb) { this@channelFlow.send(it) }
                }
            }
            .buffer(8)
            .collect { deferred ->
                val gameBasic = deferred.await()
                if (gameBasic != null)
                    this@channelFlow.send(gameBasic)
            }

    }

    //import single game
    fun addGameByUrl(url: String): Flow<Result<GameBasic>> = flow {
        val gameBasic = addGameAndEmit(url) { this@flow.emit(it) }
        if (gameBasic != null) emit(gameBasic)
    }

    private suspend fun addGameAndEmit(
        url: String,
        blurb : String? = null,
        withLocalInfo:Boolean = false,
        emit: suspend (Result<GameBasic>) -> Unit
    ): Result<GameBasic>? {
        if (gameLocalDataSource.existGame(url)) {
            emit(Result.failure(Exception("Game already exists")))
            return null
        }
        try {
            //find tempGame first
            val gameFull = tempGames.find { it.url == url }
                ?: gameRemoteDataSource.fetchGameData(url).map { it.toGameFull() }
                    .getOrThrow()
            latestGames.add(gameFull)//add to member variable
            gameLocalDataSource.insertGames(gameFull)//insert to database
            //always init localInfo when add a new game
            val localInfo: LocalInfo
            if (withLocalInfo) {
                localInfo = getLocalInfo(url)
            } else {
                localInfo = LocalInfo(
                    url,
                    blurb = blurb,//nullable
                    lastPlayedVersion = null,
                    lastPlayedTime = null,
                    starred = false
                )
                gameLocalDataSource.insertLocalInfo(localInfo)
            }
            Log.i("GameRepository", "added localInfo with $blurb")
            return Result.success(gameFull.toBasic(localInfo))
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

    suspend fun syncGameBasic(): List<GameBasic> {
        return latestGames.map { gameFull ->
            gameFull.toBasic(getLocalInfo(gameFull.url))
        }
    }
    fun size() = latestGames.size

    suspend fun getGameFull(url: String, refresh: Boolean = false): Game {
        val game = latestGames.find { it.url == url }
        return if (refresh || game == null) {
            //fetch and update to temp
            //wont insert to database
            tempGames.find { it.url == url } ?: gameRemoteDataSource.fetchGameData(url).getOrThrow().toGameFull().also { tempGames.add(it) }
        } else {
            game
        }
    }

    suspend fun getLocalInfo(url: String):LocalInfo{
        //logically,it wont be null
        return gameLocalDataSource.getLocalInfo(url)!!
    }

    suspend fun getAllLocalInfo():List<LocalInfo>{
        return gameLocalDataSource.getAllLocalInfo()
    }

    suspend fun updateLocalInfo(url:String, blurb:String? = null, lastPlayedVersion:String? = null, lastPlayedTime: LocalDateTime? = null, starred:Boolean? = null): LocalInfo {
        val oldLocalInfo = getLocalInfo(url)
        val updatedLocalInfo = oldLocalInfo.copy(
            blurb = blurb ?: oldLocalInfo.blurb,
            lastPlayedVersion = lastPlayedVersion ?: oldLocalInfo.lastPlayedVersion,
            lastPlayedTime = lastPlayedTime ?: oldLocalInfo.lastPlayedTime,
            starred = starred ?: oldLocalInfo.starred
        )
        gameLocalDataSource.updateLocalInfo(updatedLocalInfo)
        return updatedLocalInfo
    }

    suspend fun existLocalGame(url: String): Boolean {
        return gameLocalDataSource.existGame(url)
    }
}