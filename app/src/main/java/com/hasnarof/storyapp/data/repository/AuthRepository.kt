package com.hasnarof.storyapp.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.hasnarof.storyapp.data.preferences.AuthPreferences
import com.hasnarof.storyapp.data.remote.response.LoginResponse
import com.hasnarof.storyapp.data.remote.response.LoginResult
import com.hasnarof.storyapp.data.remote.retrofit.ApiService
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val pref: AuthPreferences,
    private val apiService: ApiService
) {

    suspend fun login(email: String, password: String) {
        val response = apiService.login(email, password)
        pref.setCurrentUser(
            response.loginResult.name,
            response.loginResult.userId,
            response.loginResult.token
        )
    }

    fun getCurrentUser(): Flow<LoginResult> {
        return pref.getCurrentUser()
    }

    suspend fun logout() {
        pref.setCurrentUser("","","")
    }

    companion object {
        private const val TAG = "AuthRepository"

        @Volatile
        private var instance: AuthRepository? = null
        fun getInstance(
            pref: AuthPreferences,
            apiService: ApiService
        ): AuthRepository =
            instance ?: synchronized(this) {
                instance ?: AuthRepository(pref, apiService)
            }.also { instance = it }
    }
}