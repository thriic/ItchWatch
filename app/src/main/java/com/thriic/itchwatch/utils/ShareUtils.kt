package com.thriic.itchwatch.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.thriic.core.local.LocalDateTimeConverter
import com.thriic.core.model.LocalInfo
import com.thriic.core.model.Platform

import java.io.BufferedReader
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val gson: Gson = GsonBuilder()
    .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeConverter())
    .create()
fun Context.share(content: List<LocalInfo>) {
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val formattedDate = currentDate.format(formatter)
    val file = File(externalCacheDir, "data${formattedDate}.iw")
    val json = gson.toJson(content)
    file.writeText(json)
    if (file.exists()) {
        val share = Intent(Intent.ACTION_SEND)
        val contentUri = FileProvider.getUriForFile(
            this,
            applicationContext.packageName + ".fileprovider",
            file
        )
        share.putExtra(Intent.EXTRA_STREAM, contentUri)
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        share.type = "text/plain"
        share.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(Intent.createChooser(share, "share data"))
    } else {
        Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show()
    }
}

fun Intent.readFromShare(context: Context):List<LocalInfo>? {
    Log.i("share", "action:$action type:$type")

    if (action != Intent.ACTION_SEND && action != Intent.ACTION_VIEW) return null

    val uri = this.data//parcelable<Uri>(Intent.EXTRA_STREAM)
    Log.i("share", "uri:$uri")

    if (uri != null) {
        val inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream != null) {
            val plain = BufferedReader(inputStream.reader()).readText()
            inputStream.close()
            println(plain)
            return plain.toLocalInfos()
        }
    }
    return null
}

fun String.toLocalInfos():List<LocalInfo>{
    try {
        val listType = object : TypeToken<List<LocalInfo>>() {}.type
        return gson.fromJson(this, listType)
    } catch (_: Exception) {
        throw Exception("error data format")
    }
}