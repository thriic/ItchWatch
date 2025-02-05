package com.thriic.core.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.thriic.core.model.SearchSortType
import com.thriic.core.model.SortType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferences @Inject constructor(private val context:Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

    suspend fun saveSortTypes(types: Set<SortType>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_TYPES] = types.map { it.name }.toSet()
        }
    }
    val sortTypesFlow: Flow<Set<SortType>> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SORT_TYPES]?.mapNotNull { typeName ->
                try {
                    SortType.valueOf(typeName)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }?.toSet() ?: emptySet()
        }


    suspend fun saveSearchSortType(type: SearchSortType) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SEARCH_SORT_TYPE] = type.name
        }
    }
    val searchSortTypeFlow: Flow<SearchSortType> = context.dataStore.data
        .map { preferences ->
            try {
                preferences[PreferencesKeys.SEARCH_SORT_TYPE]?.let {
                    SearchSortType.valueOf(it)
                } ?: SearchSortType.Popular
            } catch (e: IllegalArgumentException) {
                SearchSortType.Popular
            }
        }


    suspend fun saveTimeFormat(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SETTING_TIME_FORMAT] = value
        }
    }
    val timeFormatFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SETTING_TIME_FORMAT] ?: false
        }
}

object PreferencesKeys {
    val SORT_TYPES = stringSetPreferencesKey("sort_types")
    val SEARCH_SORT_TYPE = stringPreferencesKey("search_sort_type")
    val SETTING_TIME_FORMAT = booleanPreferencesKey("time_format")
}