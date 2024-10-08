package com.thriic.itchwatch.ui.utils

import android.content.Context
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

fun InputStream.readTextFile(): String {
    val reader = BufferedReader(InputStreamReader(this))
    return reader.use { it.readText() }
}