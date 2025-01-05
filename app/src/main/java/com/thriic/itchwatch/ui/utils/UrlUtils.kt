package com.thriic.itchwatch.ui.utils

import com.thriic.core.model.Tag
import com.thriic.core.model.TagType
import com.thriic.core.model.filter
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * check if url is https://xxx.itch.io/xxx
 */
fun String.isGamePageUrl(): Boolean {
    return this.matches(Regex("^https://([^/]+)\\.itch\\.io/([^/]+)\$"))
}

fun String.isCollectionUrl(): Boolean {
    return this.matches(Regex("^https://itch\\.io/c/\\d+/[a-zA-Z0-9-]+\$"))
}

fun String.cleanUrl(): String {
    val url = if (this.startsWith("https://")) this else "https://$this"
    return url.substringBefore("?")
}

fun String.encodeUrl(): String = URLEncoder.encode(this, StandardCharsets.UTF_8.toString())
fun String.decodeUrl(): String = URLDecoder.decode(this, StandardCharsets.UTF_8.toString())

data class Href(val url: String, val display: String)

fun List<Tag>.getHref(): List<Pair<String, String>> {
    return this.filter(TagType.Link).map {
        Pair(it.url, it.displayName)
    }
}
//1
//https://x.com/PUNKCAKE_delice   https://x.com/ThriveNugget
//https://ko-fi.com/nickycase
//patreon

//2
//https://bsky.app/profile/punkcake.club
//https://www.reddit.com/r/TheBibites/

//discord
//https://discord.gg/QwbDTgQm
//https://www.instagram.com/roryyaya/
//https://www.youtube.com/channel/UCjJEUMnBFHOP2zpBc7vCnsA ntytb
const val specifyDomain = "x|twitter|ko-fi|patreon|bsky"
val otherDomain = listOf("discord", "instagram", "youtube", "reddit", "bsky", "t.me", "telegram")

fun List<Pair<String, String>>.phraseSocialUrl(): List<Href> {
    val urlRegex = """(https?://\S+)""".toRegex()

    // 找到所有匹配的链接并返回列表
    return this.groupBy { it.first }
        .map { (_, group) ->
            group.maxByOrNull { if (it.second.isNotBlank()) 1 else 0 }!!
        }.mapNotNull { (url, display) ->
            val result = """(https?://)?(www.)?($specifyDomain)\.(com|app)/\S+""".toRegex().find(url)
            if (result != null) {
                val domain = result.groupValues[3].let {
                    when (it) {
                        "x" -> "twitter"
                        "bsky" -> "bluesky"
                        else -> it
                    }
                }
                val id = result.value.substringAfterLast("/")//result.groupValues[4]

                Href(url, "$domain@$id".capitalizeFirstLetter())
            } else if (otherDomain.any { keyword -> url.contains(keyword, ignoreCase = true) }) {
                val domain =
                    otherDomain.first { keyword -> url.contains(keyword, ignoreCase = true) }
                        .let { if (it == "t.me") "telegram" else it }
                //display is url || display is blank
                if (urlRegex.matches(display) || display.isBlank())
                    Href(url, domain.capitalizeFirstLetter())
                //display contains domain
                else if (display.contains(domain, ignoreCase = true))
                    Href(url, display)
                else
                    Href(url, "$display(${domain.capitalizeFirstLetter()})")
            } else {
                null
            }
        }
}

fun String.capitalizeFirstLetter(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }
}

fun String.getId(): String {
    return this.replace("[?=./\"]".toRegex(), "")
}