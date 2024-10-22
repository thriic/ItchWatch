package com.thriic.core.network.model

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.model.MetaData
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.select.Elements
import com.google.gson.Gson
import com.thriic.core.model.Tag
import com.thriic.core.model.TagType
import com.thriic.core.network.NetworkException


data class GameApiModel(
    val productApiModel:ProductApiModel,
    val icon: String?,
    val image: String?,
    val url: String,
    val devRss: String?,
    val tagElements: Elements,
    val content: String?,
    val downloadElements: Elements?,
    val devLogElements: Elements?
)
//screenshots

fun Document.toGameApiModel(url: String): GameApiModel {
    val metadata: MetaData = Ksoup.parseMetaData(element = this)

    //parse information from "Product" json
    val scripts = select("script[type=application/ld+json]")
    val products = scripts.filter { script ->
        script.html().contains("\"@type\":\"Product\"")
    }.also { if (it.isEmpty()) throw NetworkException.ParsingError("game information not found") }
    val gson = Gson()
    val productApiModel = gson.fromJson(products[0].html(), ProductApiModel::class.java)

    //parse information from "More information" panel
    val infoPanel = selectFirst("div.game_info_panel_widget.base_widget")?.selectFirst("tbody")
        ?: throw NetworkException.ParsingError("information panel not found")
    val trElements = infoPanel.select("tr")
    if(trElements.isEmpty()) throw NetworkException.ParsingError("information panel not found")

    //parse information from "Download"
    val downloadElements = selectFirst("div.upload_list_widget.base_widget")?.select("div.upload_name")

    //parse information from "Dev Log"
    val devLogElements = selectFirst("section.game_devlog")?.select("li")

    //parse content
    val content = selectFirst("div.formatted_description")?.html()


    return GameApiModel(
        productApiModel = productApiModel,
        icon = metadata.favicon,
        image = metadata.ogImage ?: metadata.twitterImage,
        url = url,
        devRss = selectFirst("link[type=application/rss+xml]")?.attr("href"),
        tagElements = trElements,
        content = content,
        downloadElements = downloadElements,
        devLogElements = devLogElements
    )

}

//post(user,url,body_html,post_number,create_at)
//"posts": [{
//                                            "user": {
//                                                "id": 5102105,
//                                                "avatar_url2x": "\/static\/images\/frog.png",
//                                                "name": "gayharborseal",
//                                                "avatar_url": "\/static\/images\/frog.png",
//                                                "url": "https:\/\/itch.io\/profile\/gayharborseal"
//                                            },
//                                            "down_votes": 0,
//                                            "vote_types": "ud",
//                                            "can_reply": true,
//                                            "url": "https:\/\/itch.io\/post\/10971732",
//                                            "id": 10971732,
//                                            "depth": 1,
//                                            "body_html": "<p>ive had SO much trouble trying to wrap my head around vtuber programs but this one is SO easy and makes so much sense. thank u so much i owe u my life<\/p>",
//                                            "can_report": true,
//                                            "post_number": 695,
//                                            "created_at": "2024-09-22 23:43:58",
//                                            "up_votes": 0
//                                        },