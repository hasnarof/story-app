package com.hasnarof.storyapp.ui.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.gson.Gson
import com.hasnarof.storyapp.Injection
import com.hasnarof.storyapp.data.remote.response.LoginResult
import com.hasnarof.storyapp.data.repository.AuthRepository
import com.hasnarof.storyapp.data.repository.StoryRepository
import com.hasnarof.storyapp.domain.model.Story
import kotlinx.coroutines.launch

class HomeViewModel (
    private val storyRepository: StoryRepository,
    private val authRepository: AuthRepository
    ) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    companion object {
        private const val TAG = "HomeViewModel"
    }

    val user: LiveData<LoginResult> = authRepository.getCurrentUser().asLiveData()

    fun getStories(token: String): LiveData<PagingData<Story>> =
        storyRepository.getStory("Bearer $token")

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }


}

class HomeViewModelFactory (private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(
                Injection.provideStoryRepository(context),
                Injection.provideAuthRepository(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}