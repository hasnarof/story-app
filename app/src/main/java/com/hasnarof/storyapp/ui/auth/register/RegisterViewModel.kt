package com.hasnarof.storyapp.ui.auth.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.hasnarof.storyapp.data.remote.response.ErrorResponse
import com.hasnarof.storyapp.data.remote.response.RegisterResponse
import com.hasnarof.storyapp.data.remote.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isSuccessRegister = MutableLiveData<Boolean>()
    val isSuccessRegister: LiveData<Boolean> = _isSuccessRegister

    companion object {
        private const val TAG = "RegisterViewModel"
    }
    
    fun register(name: String, email: String, password: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().register(name, email, password)
        client.enqueue(object: Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _isSuccessRegister.value = true

                    _message.value = "Success register"
                } else {
                    val errorResponse: ErrorResponse? = Gson().fromJson(response.errorBody()?.charStream(), ErrorResponse::class.java)
                    val errorMessage = errorResponse?.message ?: "Unknown Error"

                    _message.value = errorMessage
                    Log.e(TAG, "onFailure: $errorMessage")
                }

            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                _isLoading.value = false
                _message.value = "There is an error. Please try again later."
            }

        })
    }
}