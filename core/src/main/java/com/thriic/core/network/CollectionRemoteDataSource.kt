package com.thriic.core.network

import com.fleeksoft.ksoup.Ksoup
import com.google.gson.Gson
import com.thriic.core.network.model.CollectionApiModel
import com.thriic.core.network.model.CollectionJSON
import com.thriic.core.network.model.DownloadUrl
import com.thriic.core.network.model.GameApiModel
import com.thriic.core.network.model.GameCell
import com.thriic.core.network.model.getGameCells
import com.thriic.core.network.model.toCollectionApiModel
import com.thriic.core.network.model.toGameApiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject

class CollectionRemoteDataSource @Inject constructor(private val client: OkHttpClient) {
    /**
     * @param url collection page url
     */
    suspend fun fetchCollection(url: String): Result<CollectionApiModel> {
        return runCatching {
            withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url(url)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val text = response.body?.string() ?: throw IOException()
                    Ksoup.parse(html = text).toCollectionApiModel(url)
                }
            }
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }

    /**
     * @param url collection page url
     */
    suspend fun fetchCollectionJSON(url: String, page: Int = 2): Result<List<GameCell>> {
        return runCatching {
            withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url("$url?page=$page&format=json")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val text = response.body?.string() ?: throw IOException()
                    Ksoup.parse(html = Gson().fromJson(text, CollectionJSON::class.java).content).getGameCells()
                }
            }
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }
}