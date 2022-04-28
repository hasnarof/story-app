package com.hasnarof.storyapp.ui.auth.login

import android.content.Context
import androidx.lifecycle.*
import com.hasnarof.storyapp.Injection
import com.hasnarof.storyapp.data.Resource
import com.hasnarof.storyapp.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun login(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val response = authRepository.login(email, password)
            when(response) {
                is Resource.Success -> {
                    _isLoading.value = false
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

    companion object
}

class LoginViewModelFactory (private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(
                Injection.provideAuthRepository(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}
