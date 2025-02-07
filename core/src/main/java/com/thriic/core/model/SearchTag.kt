package com.thriic.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SearchTag(
    val displayName: String,
    @PrimaryKey val tagName: String,
    val classification: String
) {
    override fun toString(): String {
        return "[$displayName]($classification/$tagName)"
    }
}
fun SearchTag.containsTag(keyword:CharSequence): Boolean = this.tagName.contains(keyword.trim(), ignoreCase = true)

enum class SearchSortType {
    Popular, NewAPopular, TopSellers, TopRated, MostRecent
}
fun List<SearchTag>.resortByTagName(allTags: List<SearchTag>): List<SearchTag> {
    return allTags
        .filter { this.contains(it) }
        .sortedWith { tag1, tag2 ->
            when {
                tag1.tagName.contains("genre", ignoreCase = true) && !tag2.tagName.contains("genre", ignoreCase = true) -> -1
                !tag1.tagName.contains("genre", ignoreCase = true) && tag2.tagName.contains("genre", ignoreCase = true) -> 1
                else -> 0
            }
        }
}