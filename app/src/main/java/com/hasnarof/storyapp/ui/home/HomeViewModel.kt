package com.hasnarof.storyapp.ui.home

import android.content.Context
import androidx.lifecycle.*
import androidx.paging.PagingData
import com.hasnarof.storyapp.Injection
import com.hasnarof.storyapp.data.Resource
import com.hasnarof.storyapp.data.remote.response.LoginResult
import com.hasnarof.storyapp.data.repository.AuthRepository
import com.hasnarof.storyapp.data.repository.StoryRepository
import com.hasnarof.storyapp.domain.model.Story
import kotlinx.coroutines.launch

class HomeViewModel (
    private val storyRepository: StoryRepository,
    private val authRepository: AuthRepository
    ) : ViewModel() {

    private val _stories = MutableLiveData<List<Story>>()
    val stories: LiveData<List<Story>> = _stories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    val user: LiveData<LoginResult> = authRepository.getCurrentUser().asLiveData()

    fun getStories(token: String): LiveData<PagingData<Story>> =
        storyRepository.getStory(token)

    fun getStoriesWithLocation(token: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val response = storyRepository.getStoriesWithLocation(token)
            when(response) {
                is Resource.Success -> {
                    val storiesTemp = ArrayList<Story>()
                    response.data?.listStory?.map {
                        storiesTemp.add(
                            Story(it.id, it.name, it.description, it.photoUrl, it.createdAt,
                                it.lon, it.lat
                            )
                        )
                    }

                    _isLoading.value = false
                    _stories.value = storiesTemp
                }
                is Resource.ResponseError -> {
                    _isLoading.value = false
                    _message.value = response.errorResponse?.message ?: "An error occured"
                } else -> {
                _isLoading.value = false
                _message.value = "An error occured"
            }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    companion object


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