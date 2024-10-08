package com.thriic.core.network.model

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.ported.exception.IOException
import com.thriic.core.model.Platform
import com.thriic.core.model.platformMap

class SearchApiModel(
    val items: List<SearchResult>,
    val total: Int
)

data class SearchResult(
    /**
     * game link https://xxx.itch.io/xxx
     */
    val gameLink: String,
    val title: String,
    val image: String?,
    /**
     * only author name, not a link
     *
     * author link is relative to gameLink
     */
    val author: String,
    val verifiedAuthor: Boolean,
    val description: String?,
    val price: String?,
    val genre: String?,
    /**
     * may empty
     */
    val platforms: List<Platform>
)

fun Document.toSearchApiModel(): SearchApiModel {
    val gameGrid = selectFirst("div.game_grid_widget")
        ?: //check if the search returns empty
        if(selectFirst("div.empty_message") != null) return SearchApiModel(emptyList(),0)
        else throw IOException("game grid not found")
    val games = gameGrid.select("div.game_cell")
    println(games.size)
    val searchResults = games.map { game ->
        val gameThumb = game.selectFirst("a[data-action=game_grid].thumb_link.game_link")!!
        val gameData = game.selectFirst("div.game_cell_data")!!

        val platformHtml = gameData.selectFirst("div.game_platform")?.html()
        val platforms = mutableListOf<Platform>()
        if (platformHtml != null) {
            for ((platformName, platform) in platformMap) {
                if (platformHtml.contains(platformName,ignoreCase = true)) {
                    platforms.add(platform)
                }
            }
        }
        val author = gameData.selectFirst("div.game_author")!!
        SearchResult(
            gameLink = gameThumb.attr("href"),
            title = gameData.selectFirst("a.title.game_link")!!.text(),
            description = gameData.selectFirst("div.game_text")?.text(),
            image = gameThumb.selectFirst("img.lazy_loaded")?.attr("data-lazy_src"),
            author = author.selectFirst("a[data-action=game_grid]")!!.text(),
            verifiedAuthor = author.selectFirst("svg.svgicon.icon_verified") != null,
            price = gameData.selectFirst("div.price_value")?.text(),
            genre = gameData.selectFirst("div.game_genre")?.text(),
            platforms = platforms
        )
    }
    return SearchApiModel(items = searchResults, total = searchResults.size)
}