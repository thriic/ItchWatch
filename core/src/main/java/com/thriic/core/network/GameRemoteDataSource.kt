package com.thriic.core.network


import com.fleeksoft.ksoup.Ksoup
import com.thriic.core.network.model.GameApiModel
import com.thriic.core.network.model.toGameApiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject

class GameRemoteDataSource @Inject constructor(private val client: OkHttpClient) {

//    private val cacheControl by lazy {
//        CacheControl.Builder().maxStale(8, TimeUnit.HOURS).build()
//    }

    /**
     * @param url game page link
     */
    suspend fun fetchGameData(url: String): Result<GameApiModel> {
        return runCatching {
            withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url(url)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val text = response.body?.string() ?: throw IOException()
                    Ksoup.parse(html = text).toGameApiModel(url)
                }
            }
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }
}

