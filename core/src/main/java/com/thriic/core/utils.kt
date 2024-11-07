package com.thriic.core

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale


fun String.toLocalDateTime(rfcFormat:Boolean = false): LocalDateTime {
    val formatter = if(rfcFormat) DateTimeFormatter.RFC_1123_DATE_TIME else DateTimeFormatter.ofPattern("dd MMMM yyyy @ HH:mm 'UTC'")

    val utcDateTime = LocalDateTime.parse(this, formatter.withLocale(Locale.ENGLISH))
    val utcZonedDateTime = utcDateTime.atZone(ZoneId.of("UTC"))

    val localZonedDateTime = utcZonedDateTime.withZoneSameInstant(ZoneId.systemDefault())

    return localZonedDateTime.toLocalDateTime()
}


fun LocalDateTime.formatTime(): String {
    val now = LocalDateTime.now()
    val daysDifference = ChronoUnit.DAYS.between(this.toLocalDate(), now.toLocalDate()).toInt()

    val formatter: DateTimeFormatter = when {
        daysDifference < 1 -> DateTimeFormatter.ofPattern("HH:mm")
        daysDifference < 365 -> DateTimeFormatter.ofPattern("MM:dd")
        else -> DateTimeFormatter.ofPattern("yyyy:MM:dd")
    }

    return this.format(formatter)
}

fun LocalDateTime.formatTimeDifference(): String {
    val now = LocalDateTime.now()
    val minutesDifference = ChronoUnit.MINUTES.between(this, now)
    val hoursDifference = ChronoUnit.HOURS.between(this, now)
    val daysDifference = ChronoUnit.DAYS.between(this, now)
    val monthsDifference = ChronoUnit.MONTHS.between(this, now)
    val yearsDifference = ChronoUnit.YEARS.between(this, now)

//    return when {
//        minutesDifference < 60 -> "$minutesDifference minutes ago"
//        hoursDifference < 24 -> "$hoursDifference hours ago"
//        daysDifference < 30 -> "$daysDifference days ago"
//        monthsDifference < 12 -> "$monthsDifference months ago"
//        else -> "$yearsDifference years ago"
//    }
    return when {
        hoursDifference < 1 -> "$minutesDifference minutes ago"
        daysDifference < 1 -> "$hoursDifference hours ago"
        monthsDifference < 1 -> "$daysDifference days ago"
        yearsDifference < 1 -> "$monthsDifference months ago"
        else -> "$yearsDifference years ago"
    }
}

class LocalDateTimeConverter : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDateTime {
        return LocalDateTime.parse(json?.asString, formatter)
    }

    override fun serialize(
        src: LocalDateTime?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(formatter.format(src))
    }
}

