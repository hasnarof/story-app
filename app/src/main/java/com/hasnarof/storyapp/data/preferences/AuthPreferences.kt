package com.hasnarof.storyapp.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hasnarof.storyapp.data.remote.response.LoginResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthPreferences private constructor(private val dataStore: DataStore<Preferences>) {

    private val KEY_ID = stringPreferencesKey("id")
    private val KEY_NAME = stringPreferencesKey("name")
    private val KEY_TOKEN = stringPreferencesKey("token")

    fun getCurrentUser(): Flow<LoginResult> {
        return dataStore.data.map { preferences ->
            LoginResult(
                name = preferences[KEY_NAME] ?: "",
                userId = preferences[KEY_ID] ?: "",
                token = preferences[KEY_TOKEN] ?: ""
            )
        }
    }

    suspend fun setCurrentUser(name: String, userId: String, token: String) {
        dataStore.edit { preferences ->
            preferences[KEY_NAME] = name
            preferences[KEY_ID] = userId
            preferences[KEY_TOKEN] = token
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AuthPreferences? = null

        fun getInstance(dataStore: DataStore<Preferences>): AuthPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = AuthPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}