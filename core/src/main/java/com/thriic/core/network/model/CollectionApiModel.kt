package com.thriic.core.network.model

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.model.MetaData
import com.fleeksoft.ksoup.nodes.Document
import com.thriic.core.network.NetworkException

data class ResponseJSON(val page: Int, val content: String, val num_items: Int)

data class CollectionApiModel(
    val gameCells: List<GameCell>,
    val title: String,
    val url: String,
    val description: String?
)

data class GameCell(val name: String, val url: String, val blurb: String?)

fun Document.toCollectionApiModel(url: String): CollectionApiModel {
    val metadata: MetaData = Ksoup.parseMetaData(element = this)

    val gameCells = mutableListOf<GameCell>()
    val gameCellElements =
        selectFirst("div.collection_game_grid_widget.game_grid_widget")?.select("div.game_cell")
        //TODO support list collection
            //?: selectFirst("div.collection_game_list_widget.game_grid_widget")?.select("div.game_cell")
            ?: throw NetworkException.ParsingError("game not found")
    val description = selectFirst("div.formatted.collection_description")?.text()
    gameCellElements.forEach { gameCellElement ->
        val gameData = gameCellElement.selectFirst("div.game_cell_data")
        if (gameData != null) {
            val aElement = gameData.selectFirst("a.title.game_link")!!
            //TODO support html blurb
            val blurb = gameData.selectFirst("div.blurb_drop")?.text()
            gameCells.add(
                GameCell(
                    name = aElement.text(),
                    url = aElement.attr("href"),
                    blurb = blurb
                )
            )
        }
    }
    return CollectionApiModel(
        gameCells = gameCells,
        title = metadata.ogTitle ?: metadata.twitterTitle ?: metadata.title!!,
        url = url,
        description = description
    )
}

fun Document.getGameCells(): List<GameCell> {
    val gameCells = mutableListOf<GameCell>()
    val gameCellElements = select("div.game_cell")
        ?: throw NetworkException.ParsingError("game not found")
    gameCellElements.forEach { gameCellElement ->
        val gameData = gameCellElement.selectFirst("div.game_cell_data")
        if (gameData != null) {
            val aElement = gameData.selectFirst("a.title.game_link")!!
            //TODO support html blurb
            val blurb = gameData.selectFirst("div.blurb_drop")?.text()
            gameCells.add(
                GameCell(
                    name = aElement.text(),
                    url = aElement.attr("href"),
                    blurb = blurb
                )
            )
        }
    }
    return gameCells
}