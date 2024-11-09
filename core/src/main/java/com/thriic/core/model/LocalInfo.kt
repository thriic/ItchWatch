package com.thriic.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class LocalInfo(@PrimaryKey val url: String, val blurb:String)