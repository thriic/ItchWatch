package com.thriic.core.network.model

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.ported.exception.IOException
import com.thriic.core.model.Platform
import com.thriic.core.model.Rating
import com.thriic.core.model.SearchTag
import com.thriic.core.model.platformMap

class SearchApiModel(
    val items: List<SearchResult>,
    val total: Int
)

data class SearchResult(
    /**
     * game link https://xxx.itch.io/xxx
     */
    val url: String,
    val name: String,
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
    val rating: Rating? = null,
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
        val ratingHtml = gameData.selectFirst("div.game_rating")
        var rating:Rating? = null
        if(ratingHtml!=null){
            val ratingValue = ratingHtml.selectFirst("div.star_fill")!!.attr("style").substringAfter("width:").substringBefore("%").trim().toDouble()
            val ratingCount = ratingHtml.selectFirst("span.rating_count")!!.text().substringAfter("(").substringBefore("total").replace(",","").trim()
            rating = Rating("%.1f".format(ratingValue * 5 / 100), ratingCount.toInt(),ratingValue)
        }

        val price = gameData.selectFirst("div.price_value")?.text()
        val saleTag = gameData.selectFirst("div.sale_tag")?.text()
        val priceText = when {
            price == null -> null
            saleTag == null -> price
            else -> "$price($saleTag)"
        }
        SearchResult(
            url = gameThumb.attr("href"),
            name = gameData.selectFirst("a.title.game_link")!!.text(),
            description = gameData.selectFirst("div.game_text")?.text(),
            image = gameThumb.selectFirst("img.lazy_loaded")?.attr("data-lazy_src"),
            author = author.selectFirst("a[data-action=game_grid]")!!.text(),
            verifiedAuthor = author.selectFirst("svg.svgicon.icon_verified") != null,
            price = priceText,
            genre = gameData.selectFirst("div.game_genre")?.text(),
            rating = rating,
            platforms = platforms
        )
    }
    return SearchApiModel(items = searchResults, total = searchResults.size)
}

fun Document.parseSearchTags():List<SearchTag>{
    val tagElements = selectFirst("select.tag_selector")?.select("option") ?: throw Exception("cannot fetch tags")
    val tags = mutableListOf<SearchTag>()
    tagElements.forEach {
        val url = it.attr("value")
        if(url.isNotBlank()){
            val displayName = it.text()
            val classification = url.substringAfter("/").substringBeforeLast("/")
            val tagName = url.substringAfterLast("/")
            tags.add(SearchTag(displayName = displayName, tagName = tagName, classification = classification))
        }
    }
    return tags
}