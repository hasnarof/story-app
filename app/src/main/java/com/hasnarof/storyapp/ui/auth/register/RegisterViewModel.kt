package com.hasnarof.storyapp.ui.auth.register

import android.content.Context
import androidx.lifecycle.*
import com.hasnarof.storyapp.Injection
import com.hasnarof.storyapp.data.Resource
import com.hasnarof.storyapp.data.repository.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isSuccessRegister = MutableLiveData<Boolean>()
    val isSuccessRegister: LiveData<Boolean> = _isSuccessRegister

    fun register(name: String, email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val response = authRepository.register(name, email, password)
            when(response) {
                is Resource.Success -> {
                    _isLoading.value = false
                    _message.value = "Success register"
                    _isSuccessRegister.value = true
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
}

class RegisterViewModelFactory (private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(
                Injection.provideAuthRepository(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}