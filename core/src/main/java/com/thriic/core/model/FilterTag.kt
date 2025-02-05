package com.thriic.core.model

import androidx.room.Entity

@Entity
data class FilterTag(val displayName: String, val url: String, val type: TagType) {
    val tagName: String
        get() = if (type == TagType.Link || type == TagType.Author) url else url.substringAfterLast(
            "/"
        )

    override fun equals(other: Any?): Boolean {
        if (other !is FilterTag) return false
        return other.displayName == displayName && other.type == type && other.tagName == tagName
    }

    override fun hashCode(): Int {
        var result = tagName.hashCode()
        result *= 31 + displayName.hashCode()
        return result
    }

    override fun toString(): String {
        return "[$displayName]($url)"
    }
}

fun List<FilterTag>.filter(vararg types: TagType): List<FilterTag> {
    return this.filter { types.contains(it.type) }
}

@Entity
enum class TagType {
    Status, Platform, Author, MadeWith, NormalTag, Duration, Language, Input, Category, Genre, Link
}


enum class SortType {
    Name,
    Time,
    TimeReverse,
    Starred,
    Updated
}