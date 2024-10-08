package com.thriic.core.network

import com.prof18.rssparser.RssParserBuilder
import com.thriic.core.network.model.DevLog
import com.thriic.core.network.model.toDevLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

class DevLogRemoteDataSource @Inject constructor(private val client: OkHttpClient) {

    suspend fun fetchDevLog(url: String): Result<DevLog> {
        return runCatching {
            withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url(url)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw NetworkException.NetworkError(response)
                    val text = response.body?.string() ?: throw NetworkException.ParsingError("dev log")
                    val builder = RssParserBuilder(
                        callFactory = client,
                        charset = Charsets.UTF_8,
                    )
                    val rssParser = builder.build()
                    //the format of dev log publish time between game page and dev log rss is different
                    rssParser.parse(text).toDevLog()
                }
            }
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }
}