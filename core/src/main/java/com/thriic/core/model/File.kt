package com.thriic.core.model


data class File(val name: String, val platform: Platform, val size: String)

fun List<File>.getVersionOrFileName(): String {
    return this[0].name
}