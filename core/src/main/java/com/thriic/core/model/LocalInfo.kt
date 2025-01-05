package com.thriic.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

//when fetch latest info from itch.io, data in localInfo shouldn't be modified
@Entity
data class LocalInfo(@PrimaryKey val url: String, val blurb:String?, val lastPlayedVersion:String?, val lastPlayedTime: LocalDateTime?, val starred:Boolean)