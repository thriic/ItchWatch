package com.thriic.core.model

import com.fleeksoft.ksoup.nodes.Document
import com.thriic.core.network.NetworkException
import com.thriic.core.toLocalDateTime
import java.time.LocalDateTime


data class File(
    val name: String,
    val platform: Platform,
    val size: String,
    val uploadTime: LocalDateTime? = null
)

val zipVersionRegex = Regex(
    ".*-((a|b|v|ver.*)*[0-9.]+[a-z]*)(\\s*-\\s*public)*\\s*-\\s*(pc|mac|linux|win|windows)\\s*.\\s*(zip|tar.bz2)",
    RegexOption.IGNORE_CASE
)
val androidVersionRegex = Regex(
    ".*-((a|b|v|ver.*)*[0-9.]+[a-z]*)(\\s*-\\s*android)*\\s*.\\s*apk",
    RegexOption.IGNORE_CASE
)
val versionRegex = Regex("((v|ver.?)\\s*[0-9.]+[a-y]?|build [0-9.]{1,3})", RegexOption.IGNORE_CASE)

fun List<File>.getVersionOrFileName(): String {
    if (isEmpty()) return ""
    val windowsFile = this.find { it.platform == Platform.WINDOWS }
    //check windows file name
    if (windowsFile != null) {
        zipVersionRegex.find(windowsFile.name)?.let { res ->
            res.groups[1]?.let { return it.value }
        }
    }
    //check all files
    for (file in this) {
        if (file.platform != Platform.ANDROID) {
            zipVersionRegex.find(file.name)?.let { res ->
                res.groups[1]?.let { return it.value }
            }
            //or, match standard formatted version string
            versionRegex.find(file.name)?.let { res ->
                res.groups[1]?.let { return it.value }
            }
        } else {
            androidVersionRegex.find(file.name)?.let { res ->
                res.groups[1]?.let { return it.value }
            }
        }
    }
    return windowsFile?.name ?: this[0].name
}

fun Document.toFileWithTime(): List<File> {
    val downloads = selectFirst("div.upload_list_widget.base_widget")?.select("div.info_column")
        ?: throw NetworkException.ParsingError("downloads not found")
    val files = mutableListOf<File>()
    downloads.forEach {
        val name = it.selectFirst("strong")!!.attr("title")
        val size = it.selectFirst("span.file_size")!!.text()
        val platformHtml = it.selectFirst("span.download_platforms")!!.html()
        val type = platformMap.entries.firstOrNull { (platformName, _) ->
            platformHtml.contains(platformName, ignoreCase = true)
        }?.value ?: Platform.UNKNOWN
        val time = it.selectFirst("abbr")!!.attr("title").toLocalDateTime()
        files.add(File(name = name, platform = type, size = size, uploadTime = time))
    }
    return files
}