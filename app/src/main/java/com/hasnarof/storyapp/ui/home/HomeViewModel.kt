package com.hasnarof.storyapp.ui.home

import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.hasnarof.storyapp.data.preferences.AuthPreferences
import com.hasnarof.storyapp.data.remote.response.ErrorResponse
import com.hasnarof.storyapp.data.remote.response.LoginResult
import com.hasnarof.storyapp.data.remote.response.StoriesResponse
import com.hasnarof.storyapp.data.remote.retrofit.ApiConfig
import com.hasnarof.storyapp.domain.model.Story
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel(private val pref: AuthPreferences) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _stories = MutableLiveData<List<Story>>()
    val stories: LiveData<List<Story>> = _stories

    companion object {
        private const val TAG = "HomeViewModel"
    }

    fun getStories(token: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().stories("Bearer $token")
        client.enqueue(object: Callback<StoriesResponse> {
            override fun onResponse(call: Call<StoriesResponse>, response: Response<StoriesResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val convertedStories = ArrayList<Story>()

                    response.body()?.listStory?.forEach {
                        if (it != null) {
                            convertedStories.add(
                                Story(it.id, it.name, it.description, it.photoUrl)
                            )
                        }
                    }

                    _stories.value = convertedStories
                } else {
                    val errorResponse: ErrorResponse? = Gson().fromJson(response.errorBody()?.charStream(), ErrorResponse::class.java)
                    val errorMessage = errorResponse?.message ?: "Unknown Error"

                    _message.value = errorMessage
                    Log.e(TAG, "onFailure: $errorMessage")
                }

            }

            override fun onFailure(call: Call<StoriesResponse>, t: Throwable) {
                _isLoading.value = false
                _message.value = "There is an error. Please try again later."
            }

        })
    }

    fun getCurrentUser(): LiveData<LoginResult> {
        return pref.getCurrentUser().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            pref.setCurrentUser("","","")
        }
    }
}