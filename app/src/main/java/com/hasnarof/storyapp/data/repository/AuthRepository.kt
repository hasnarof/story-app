package com.hasnarof.storyapp.data.repository

import android.util.Log
import com.hasnarof.storyapp.data.Resource
import com.hasnarof.storyapp.data.preferences.AuthPreferences
import com.hasnarof.storyapp.data.remote.response.LoginResponse
import com.hasnarof.storyapp.data.remote.response.LoginResult
import com.hasnarof.storyapp.data.remote.response.RegisterResponse
import com.hasnarof.storyapp.data.remote.retrofit.ApiService
import com.hasnarof.storyapp.helper.getErrorResponse
import kotlinx.coroutines.flow.Flow


class AuthRepository(
    private val pref: AuthPreferences,
    private val apiService: ApiService
) {

    suspend fun register(name: String, email: String, password: String): Resource<RegisterResponse> {
        try {
            val response = apiService.register(name, email, password)
            if(response.isSuccessful) {
                val result = response.body()
                return Resource.Success(result)
            } else {
                return Resource.ResponseError(getErrorResponse(response.errorBody()!!.string()))
            }
        } catch (e: Exception) {
            Log.d(TAG, "${e.message}")
            return Resource.Error(e.message ?: "An error occured")
        }
    }

    suspend fun login(email: String, password: String): Resource<LoginResponse> {
        try {
            val response = apiService.login(email, password)
            if(response.isSuccessful) {
                val result = response.body()
                pref.setCurrentUser(
                    result?.loginResult?.name ?: "",
                    result?.loginResult?.userId ?: "",
                    result?.loginResult?.token ?: ""
                )
                return Resource.Success(result)
            } else {
                return Resource.ResponseError(getErrorResponse(response.errorBody()!!.string()))
            }
        } catch (e: Exception) {
            Log.d(TAG, "${e.message}")
            return Resource.Error(e.message ?: "An error occured")
        }
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