package com.thriic.core.model

import androidx.room.Entity

@Entity
enum class Platform(tagName:String) {
    WINDOWS("Windows"),LINUX("Linux"),MACOS("macOS"),IOS("iOS"),ANDROID("Android"),WEB("HTML5"),UNKNOWN("")
}
val platformMap = mapOf(
    "Windows" to Platform.WINDOWS,
    "Linux" to Platform.LINUX,
    "macOS" to Platform.MACOS,
    "iOS" to Platform.IOS,
    "Android" to Platform.ANDROID,
    "browser" to Platform.WEB,
    "HTML" to Platform.WEB
)
