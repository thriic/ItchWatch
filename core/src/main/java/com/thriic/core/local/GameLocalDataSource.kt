package com.thriic.core.local

import com.thriic.core.model.Game
import com.thriic.core.model.LocalInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GameLocalDataSource @Inject constructor(private val gameDao: GameDao, private val infoDao: InfoDao) {
    suspend fun getLocalGames(): List<Game> =
        withContext(Dispatchers.IO) {
            gameDao.getAll()
        }


    suspend fun insertGames(vararg games: Game) {
        withContext(Dispatchers.IO) {
            gameDao.insertAll(*games)
        }
    }

    suspend fun updateGames(vararg games: Game) {
        withContext(Dispatchers.IO) {
            gameDao.updateGames(*games)
        }
    }

    suspend fun deleteGame(game: Game) {
        withContext(Dispatchers.IO) {
            gameDao.delete(game)
        }
    }

    suspend fun existGame(url: String): Boolean {
        return withContext(Dispatchers.IO) {
            gameDao.countByUrl(url) > 0
        }
    }

    suspend fun getLocalInfo(url:String) =
        withContext(Dispatchers.IO) {
            infoDao.queryInfo(url)
        }

    suspend fun insertLocalInfo(vararg infos: LocalInfo) {
        withContext(Dispatchers.IO) {
            infoDao.insertAll(*infos)
        }
    }

    suspend fun updateLocalInfo(vararg infos: LocalInfo) {
        withContext(Dispatchers.IO) {
            infoDao.updateInfos(*infos)
        }
    }

    suspend fun deleteLocalInfo(info: LocalInfo) {
        withContext(Dispatchers.IO) {
            infoDao.delete(info)
        }
    }



}