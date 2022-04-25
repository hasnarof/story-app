package com.hasnarof.storyapp.ui.auth.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.hasnarof.storyapp.data.preferences.AuthPreferences
import com.hasnarof.storyapp.data.remote.response.ErrorResponse
import com.hasnarof.storyapp.data.remote.response.LoginResponse
import com.hasnarof.storyapp.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginViewModel(private val pref: AuthPreferences) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    companion object {
        private const val TAG = "LoginViewModel"
    }

    fun login(email: String, password: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().login(email, password)
        client.enqueue(object: Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    viewModelScope.launch {
                        pref.setCurrentUser(
                            response.body()?.loginResult?.name.toString(),
                            response.body()?.loginResult?.userId.toString(),
                            response.body()?.loginResult?.token.toString()
                        )
                    }

                    _message.value = "Success login"
                } else {
                    val errorResponse: ErrorResponse? = Gson().fromJson(response.errorBody()?.charStream(), ErrorResponse::class.java)
                    val errorMessage = errorResponse?.message ?: "Unknown Error"

                    _message.value = errorMessage
                    Log.e(TAG, "onFailure: $errorMessage")
                }

            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _isLoading.value = false
                _message.value = "There is an error. Please try again later."
            }

        })
    }
}