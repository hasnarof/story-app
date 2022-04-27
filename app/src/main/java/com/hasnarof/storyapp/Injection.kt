package com.hasnarof.storyapp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Dao
import com.hasnarof.storyapp.data.database.StoryDatabase
import com.hasnarof.storyapp.data.preferences.AuthPreferences
import com.hasnarof.storyapp.data.remote.retrofit.ApiConfig
import com.hasnarof.storyapp.data.repository.AuthRepository
import com.hasnarof.storyapp.data.repository.StoryRepository

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

object Injection {
    fun provideStoryRepository(context: Context): StoryRepository {
        val database = StoryDatabase.getDatabase(context)
        val apiService = ApiConfig.getApiService()
        return StoryRepository(database, apiService)
    }

    fun provideAuthRepository(context: Context): AuthRepository {
        val authPreference = AuthPreferences.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService()
        return AuthRepository(authPreference, apiService)
    }
}