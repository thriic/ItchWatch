package com.thriic.core.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fleeksoft.ksoup.Ksoup
import com.thriic.core.network.NetworkException
import com.thriic.core.network.model.DevLogItem
import com.thriic.core.network.model.GameApiModel
import com.thriic.core.toLocalDateTime
import java.time.LocalDateTime

/**
 * Info Page shows Game Full Information
 * @property name game name
 * @property icon website icon
 * @property updatedTime updated time on page
 * @property publishedTime published time on page
 */
@Entity
data class Game(
    val name: String,
    val icon: String?,
    @PrimaryKey val url: String,//TODO use gameId as unique identifier
    val description: String?,
    val content: String?,
    @Embedded val rating: Rating?,
    val image: String?,
    /**
     * init when fetch games
     *
     * get all devLogs with full information by fetching downloadPage
     * @see com.thriic.core.network.GameRemoteDataSource.fetchDownloadPage
     */
    val devLogs: List<DevLogItem>,
    val updatedTime: LocalDateTime?,
    val publishedTime: LocalDateTime?,
    val platforms: Set<Platform>,
    /**
     * Download files vb
     *
     * there are **no downloadable files** when a game can only be played in the browser
     *
     * init when fetch games
     *
     * get full information(upload time) by fetching devLogs from feed
     *
     * @see com.thriic.core.network.DevLogRemoteDataSource.fetchDevLog
     */
    val files: List<File>,
    val tags: List<Tag>
) {
    fun toBasic(localInfo: LocalInfo): GameBasic =
        GameBasic(
            url = url,
            name = name,
            image = image,
            updatedTime = updatedTime,
            publishedTime = publishedTime,
            versionOrFileName = files.getVersionOrFileName(),
            platforms = platforms,
            devLogs = devLogs,
            filterTags = tags.filter(TagType.Platform, TagType.NormalTag, TagType.Language),
            localInfo = localInfo
        )
}

/**
 * Lib Page shows list of GameBasic
 */
data class GameBasic(
    val url: String,
    val name: String,
    val image: String?,
    val updatedTime: LocalDateTime?,
    val publishedTime: LocalDateTime?,
    val versionOrFileName: String?,
    val platforms: Set<Platform>,
    val devLogs: List<DevLogItem>?,
    val filterTags: List<Tag>,
    val localInfo: LocalInfo
){
    val updated:Boolean
        get() = localInfo.lastPlayedVersion != null && versionOrFileName != localInfo.lastPlayedVersion
}

data class Rating(val ratingValue: String, val ratingCount: Int)

fun GameApiModel.toGameFull(): Game {
    var updatedTime: String? = null
    var publishedTime: String? = null

    val tagList = mutableListOf<Tag>()
    tagElements.forEach { tr ->
        val tds = tr.select("td")
        if (tds.size >= 2) {
            val valueElement = tds[1]
            when (tds[0].text()) {
                "Updated" -> {
                    updatedTime = valueElement.selectFirst("abbr")!!.attr("title")
                }

                "Published" -> {
                    publishedTime = valueElement.selectFirst("abbr")!!.attr("title")
                }

                "Status" -> {
                    tagList += with(valueElement.selectFirst("a")!!) {
                        Tag(text(), attr("href"), TagType.Status)
                    }
                }

                "Platforms" -> {
                    tagList += valueElement.select("a").map {
                        Tag(it.text(), it.attr("href"), TagType.Platform)
                    }
                }

                "Author", "Authors" -> {
                    tagList += valueElement.select("a").map {
                        Tag(it.text(), it.attr("href"), TagType.Author)
                    }
                }

                "Category" -> {
                    tagList += with(valueElement.selectFirst("a")!!) {
                        Tag(text(), attr("href"), TagType.Category)
                    }
                }

                "Genre" -> {
                    tagList += valueElement.select("a").map {
                        Tag(it.text(), it.attr("href"), TagType.Genre)
                    }
                }

                "Made with" -> {
                    tagList += valueElement.select("a").map {
                        Tag(it.text(), it.attr("href"), TagType.MadeWith)
                    }
                }

                "Tags" -> {
                    tagList += valueElement.select("a").map {
                        Tag(it.text(), it.attr("href"), TagType.NormalTag)
                    }
                }

                "Inputs" -> {
                    tagList += valueElement.select("a").map {
                        Tag(it.text(), it.attr("href"), TagType.Input)
                    }
                }

                "Languages" -> {
                    tagList += valueElement.select("a").map {
                        Tag(it.text(), it.attr("href"), TagType.Language)
                    }
                }

                "Average session" -> {
                    tagList += with(valueElement.selectFirst("a")!!) {
                        Tag(text(), attr("href"), TagType.Duration)
                    }
                }

                "Links" -> {
                    tagList += valueElement.select("a").map {
                        Tag(it.text(), it.attr("href"), TagType.Link)
                    }
                }

                else -> {}
            }

        } else throw NetworkException.ParsingError("failed to parse information")
    }

    val files = mutableListOf<File>()
    downloadElements?.forEach {
        val name = it.selectFirst("strong")!!.attr("title")
        val size = it.selectFirst("span.file_size")!!.text()
        val platformHtml = it.selectFirst("span.download_platforms")!!.html()
        val type = platformMap.entries.firstOrNull { (platformName, _) ->
            platformHtml.contains(platformName, ignoreCase = true)
        }?.value ?: Platform.UNKNOWN
        files.add(File(name, type, size))
    }

    val platforms = mutableSetOf<Platform>()
    if (tagList.any { it.type == TagType.Platform }) {
        tagList.filter { it.type == TagType.Platform }.forEach {
            platformMap.entries.firstOrNull { (platformName, _) ->
                it.displayName.contains(platformName, ignoreCase = true)
            }?.value?.let { platforms.add(it) }
        }
    } else if (files.isNotEmpty()) {
        files.forEach { platforms.add(it.platform) }
    }

    val devLogs = mutableListOf<DevLogItem>()
    devLogElements?.forEach {
        val aElement = it.selectFirst("a")!!
        val pubDate = it.selectFirst("abbr")!!.attr("title")
        devLogs.add(
            DevLogItem(
                title = aElement.text(),
                link = aElement.attr("href"),
                pubDate = pubDate.toLocalDateTime(),
            )
        )
    }

    return Game(
        name = productApiModel.name,
        icon = icon,
        description = productApiModel.description,
        rating = productApiModel.aggregateRating?.let {
            Rating(
                ratingCount = it.ratingCount,
                ratingValue = it.ratingValue
            )
        },
        content = content,
        image = image,
        url = url,
        devLogs = devLogs,
        updatedTime = updatedTime?.toLocalDateTime(),
        publishedTime = publishedTime?.toLocalDateTime(),
        tags = tagList,
        platforms = platforms,
        files = files
    )
}

fun getContentLinks(contentPlain: String): List<Pair<String, String>> {
    val contentElement = Ksoup.parse(html = contentPlain)
    return contentElement.select("a").map {
        Pair(it.attr("href"), it.text()).also(::println)
    }
}