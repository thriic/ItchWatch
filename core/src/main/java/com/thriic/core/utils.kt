package com.thriic.core

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import kotlinx.coroutines.delay
import java.lang.reflect.Type
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

enum class TimeFormat {
    AbsoluteDate, //yyyy/MM/dd
    SimpleRelative, //x days ago
    DetailedRelative //x years/months/days/minutes ago
}

fun String.toLocalDateTime(rfcFormat:Boolean = false): LocalDateTime {
    val formatter = if(rfcFormat) DateTimeFormatter.RFC_1123_DATE_TIME else DateTimeFormatter.ofPattern("dd MMMM yyyy @ HH:mm 'UTC'")

    val utcDateTime = LocalDateTime.parse(this, formatter.withLocale(Locale.ENGLISH))
    val utcZonedDateTime = utcDateTime.atZone(ZoneId.of("UTC"))

    val localZonedDateTime = utcZonedDateTime.withZoneSameInstant(ZoneId.systemDefault())

    return localZonedDateTime.toLocalDateTime()
}


fun LocalDateTime.formatTime(timeFormat: TimeFormat = TimeFormat.DetailedRelative): String {
    val now = LocalDateTime.now()
    return when(timeFormat) {
        TimeFormat.AbsoluteDate -> {
            val daysDifference =
                ChronoUnit.DAYS.between(this.toLocalDate(), now.toLocalDate()).toInt()

            val formatter: DateTimeFormatter = when {
                daysDifference < 1 -> DateTimeFormatter.ofPattern("HH:mm")
                else -> DateTimeFormatter.ofPattern("yyyy/MM/dd")
            }

            this.format(formatter)
        }
        TimeFormat.DetailedRelative -> {
            val minutesDifference = ChronoUnit.MINUTES.between(this, now)
            val hoursDifference = ChronoUnit.HOURS.between(this, now)
            val daysDifference = ChronoUnit.DAYS.between(this, now)
            val monthsDifference = ChronoUnit.MONTHS.between(this, now)
            val yearsDifference = ChronoUnit.YEARS.between(this, now)
            when {
                hoursDifference < 1 -> "$minutesDifference minutes ago"
                daysDifference < 1 -> "$hoursDifference hours ago"
                monthsDifference < 1 -> "$daysDifference days ago"
                yearsDifference < 1 -> "$monthsDifference months ago"
                else -> "$yearsDifference years ago"
            }
        }

        TimeFormat.SimpleRelative -> {
            val daysDifference =
                ChronoUnit.DAYS.between(this.toLocalDate(), now.toLocalDate()).toInt()
            "$daysDifference days ago"
        }
    }
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


suspend fun <T> withRetry(
    maxRetries: Int = 3,
    initialDelay: Long = 1000,
    shouldRetry: (Throwable) -> Boolean,
    block: suspend () -> T
): T {
    require(maxRetries >= 0) { "maxRetries should be >= 0" }

    var currentDelay = initialDelay
    var lastError: Throwable? = null

    repeat(maxRetries + 1) { attempt ->
        //lastError?.let { throw it }
        try {
            return block()
        } catch (e: Throwable) {
            lastError = e
            if (attempt < maxRetries && shouldRetry(e)) {
                delay(currentDelay)
                currentDelay *= 2
            }
        }
    }

    throw lastError ?: error("Unexpected state")
}