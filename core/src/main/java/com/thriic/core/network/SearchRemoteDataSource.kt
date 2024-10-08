package com.thriic.core.network

import com.fleeksoft.ksoup.Ksoup
import com.thriic.core.network.model.SearchApiModel
import com.thriic.core.network.model.toSearchApiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject

class SearchRemoteDataSource @Inject constructor(private val client: OkHttpClient) {

    /**
     * search on itch.io by keyword
     *
     * hard to use!!!
     * @param keyword search keyword
     * @param classification game,assets,game_mod,physical_game,soundtrack,tool,comic,book
     * @return search results(max 54 items)
     */
    suspend fun fetchKeywordSearch(keyword:String, classification:String = "game"): Result<SearchApiModel> {
        return runCatching {
            withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url("https://itch.io/search?type=games&classification=${classification}&q=${keyword}")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val text = response.body?.string() ?: throw IOException()
                    Ksoup.parse(html = text).toSearchApiModel()
                }
            }
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }

    //TODO get all Tags
    suspend fun fetchTagSearch(keyword:String, classification:String = "game"){}
    //https://itch.io/games/tag-endless/tag-pixel-art?page=2&format=json
    //{
    //    "content": xxx
    //    "page": 2,
    //    "num_items": 36
    //}
}