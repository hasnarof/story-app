package com.hasnarof.storyapp.ui.story.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.hasnarof.storyapp.data.preferences.AuthPreferences
import com.hasnarof.storyapp.data.remote.response.LoginResult
import com.hasnarof.storyapp.data.remote.response.StoryAddResponse
import com.hasnarof.storyapp.data.remote.retrofit.ApiConfig
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StoryAddViewModel(private val pref: AuthPreferences) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isSuccessUpload = MutableLiveData<Boolean>()
    val isSuccessUpload: LiveData<Boolean> = _isSuccessUpload

    fun uploadImage(token: String, imageMultipart: MultipartBody.Part, description: RequestBody) {
        _isLoading.value = true
        val service = ApiConfig.getApiService().addStory("Bearer $token", imageMultipart, description)

        service.enqueue(object : Callback<StoryAddResponse> {
            override fun onResponse(
                call: Call<StoryAddResponse>,
                response: Response<StoryAddResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error) {
                        _message.value = responseBody.message
                    }
                    _isSuccessUpload.value = true
                } else {
                    _message.value = response.message()
                }
            }
            override fun onFailure(call: Call<StoryAddResponse>, t: Throwable) {
                _isLoading.value = false
                _message.value = "Fail to create retrofit instance"
            }
        })
    }

    fun getCurrentUser(): LiveData<LoginResult> {
        return pref.getCurrentUser().asLiveData()
    }

}