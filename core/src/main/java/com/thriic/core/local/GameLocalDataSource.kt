package com.thriic.core.local

import com.thriic.core.model.Game
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
}