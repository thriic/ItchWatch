package com.thriic.core.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import com.thriic.core.model.File
import com.thriic.core.model.Game
import com.thriic.core.model.LocalInfo
import com.thriic.core.model.Platform
import com.thriic.core.model.Tag
import com.thriic.core.network.model.DevLogItem
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Database(entities = [Game::class,LocalInfo::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun infoDao(): InfoDao
}

@Dao
interface GameDao {
    @Query("SELECT * FROM game")
    fun getAll(): List<Game>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg games: Game)

    @Update
    fun updateGames(vararg games: Game)

    @Delete
    fun delete(game: Game)

    @Query("SELECT COUNT(*) FROM game WHERE url = :url")
    fun countByUrl(url: String): Int
}

@Dao
interface InfoDao {
    @Query("SELECT * FROM localinfo")
    fun getAll(): List<LocalInfo>

    @Query("SELECT * FROM localinfo WHERE url = :url")
    fun queryInfo(url: String): LocalInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg infos: LocalInfo)

    @Update
    fun updateInfos(vararg infos: LocalInfo)

    @Delete
    fun delete(info: LocalInfo)
}

class Converters {
    private val gson: Gson = GsonBuilder()
    .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeConverter())
    .create()
    @TypeConverter
    fun fromTimestamp(value: String): LocalDateTime {
        val type = object : TypeToken<LocalDateTime>() {}.type
        return gson.fromJson(value,type)
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime): String {
        return gson.toJson(date)
    }
    @TypeConverter
    fun stringToPlatforms(value: String): Set<Platform> {
        val listType = object : TypeToken<Set<Platform>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun platformToString(set: Set<Platform>): String {
        return gson.toJson(set)
    }

    @TypeConverter
    fun stringToDevLog(value: String): List<DevLogItem> {
        val listType = object : TypeToken<List<DevLogItem>>() {

        }.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun devLogToString(list: List<DevLogItem>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun stringToFiles(value: String): List<File> {
        val listType = object : TypeToken<List<File>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun filesToString(list: List<File>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun stringToTags(value: String): List<Tag> {
        val listType = object : TypeToken<List<Tag>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun tagsToString(list: List<Tag>): String {
        return gson.toJson(list)
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