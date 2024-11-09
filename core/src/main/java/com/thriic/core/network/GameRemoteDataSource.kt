package com.thriic.core.network


import com.fleeksoft.ksoup.Ksoup
import com.google.gson.Gson
import com.thriic.core.model.File
import com.thriic.core.model.toFileWithTime
import com.thriic.core.network.model.DownloadUrl
import com.thriic.core.network.model.GameApiModel
import com.thriic.core.network.model.toGameApiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
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

    /**
     * @param url game page link
     */
    suspend fun fetchDownloadUrl(url: String, csrfToken: String): Result<DownloadUrl> {
        return runCatching {
            withContext(Dispatchers.IO) {
                val formBody: RequestBody = FormBody.Builder()
                    .add("csrf_token", csrfToken)
                    .build()
                val request = Request.Builder()
                    .post(formBody)
                    .url("$url/download_url")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val text = response.body?.string() ?: throw IOException()
                    Gson().fromJson(text, DownloadUrl::class.java)
                    //Ksoup.parse(html = text).toGameApiModel(url)
                }
            }
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }

    /**
     * @param downloadUrl download page link
     */
    suspend fun fetchDownloadPage(downloadUrl: String, csrfToken: String): Result<List<File>> {
        return runCatching {
            withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url(downloadUrl)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val text = response.body?.string() ?: throw IOException()
                    Ksoup.parse(html = text).toFileWithTime()
                }
            }
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }
}

