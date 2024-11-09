package com.thriic.core.network.model

import com.prof18.rssparser.model.RssChannel
import com.thriic.core.network.NetworkException
import com.thriic.core.toLocalDateTime
import java.time.LocalDateTime

data class DevLog(val url: String, val items: List<DevLogItem>)

data class DevLogItem(
    val title: String,
    val link: String,
    val pubDate: LocalDateTime,
    /**
     * html
     */
    val description: String? = null
)

fun RssChannel.toDevLog(): DevLog {
    val devLogItems =
        this.items
            .filter { it.title != null && it.link != null && it.pubDate != null }
            .map {
                //TODO solve pubDate
                DevLogItem(
                    title = it.title!!, link = it.link!!, pubDate = it.pubDate!!.toLocalDateTime(true),
                    description = it.description
                )
            }
    //link here is required.
    if (devLogItems.isNotEmpty())
        return DevLog(url = link ?: throw NetworkException.ParsingError("dev log"), items = devLogItems)
    else
        throw NetworkException.ParsingError("dev log")
}
//DevLogItem(
// guid=https://arrietty.itch.io/prototype-n/devlog/795087/prototype-n-demo-v490-is-now-out,
// title=Prototype N Demo v4.9.0 is now out!!,
// link=https://arrietty.itch.io/prototype-n/devlog/795087/prototype-n-demo-v490-is-now-out,
// pubDate=Fri, 06 Sep 2024 23:23:04 GMT,
// description=<img class="post_image" src="https://img.itch.zone/aW1nLzE3Njg2MDk3LmdpZg==/400x224%23c/KF0VZM.gif"/><p>SAGExpo 2024 just launched, so naturally that means I&#039;ve prepared a new demo for such an occasion! What&#039;s New? Cutscenes! The game now has a story! Selena herself finally debuts in the demo. Now you c...</p>, content=null, image=https://img.itch.zone/aW1nLzE3Njg2MDk3LmdpZg==/400x224%23c/KF0VZM.gif,
// audio=null, video=null, sourceName=null, sourceUrl=null, categories=[devlog], itunesItemData=ItunesItemData(author=null, duration=null, episode=null, episodeType=null, explicit=null, image=null, keywords=[], subtitle=null, summary=null, season=null), commentsUrl=null
// )