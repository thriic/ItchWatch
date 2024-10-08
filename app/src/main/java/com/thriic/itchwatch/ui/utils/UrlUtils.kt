package com.thriic.itchwatch.ui.utils

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * check if url is https://xxx.itch.io/xxx
 */
fun String.isGamePageUrl(): Boolean {
    return this.matches(Regex("^https://([^/]+)\\.itch\\.io/([^/]+)\$"))
}

fun String.cleanUrl():String{
    val url = if (this.startsWith("https://")) this else "https://$this"
    return url.substringBefore("?")
}

fun String.encodeUrl() = URLEncoder.encode(this, StandardCharsets.UTF_8.toString())
fun String.decodeUrl() = URLDecoder.decode(this, StandardCharsets.UTF_8.toString())