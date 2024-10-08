package com.thriic.core.model

import androidx.room.Entity

@Entity
data class Tag(val displayName: String, val url: String, val type: TagType) {
    val tagName: String
        get() = if (type == TagType.Link || type == TagType.Author) url else url.substringAfterLast(
            "/"
        )

    override fun toString(): String {
        return "[$displayName]($url)"
    }
}

fun List<Tag>.filter(type: TagType): List<Tag> {
    return this.filter { it.type == type }
}

@Entity
enum class TagType {
    Status, Platform, Author, MadeWith, NormalTag, Duration, Language, Input, Category, Genre, Link
}