package com.hasnarof.storyapp.ui.auth.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.hasnarof.storyapp.Injection
import com.hasnarof.storyapp.data.preferences.AuthPreferences
import com.hasnarof.storyapp.data.remote.response.ErrorResponse
import com.hasnarof.storyapp.data.remote.response.LoginResponse
import com.hasnarof.storyapp.data.remote.retrofit.ApiConfig
import com.hasnarof.storyapp.data.repository.AuthRepository
import com.hasnarof.storyapp.ui.home.HomeViewModel
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception


class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    companion object {
        private const val TAG = "LoginViewModel"
    }

    fun login(email: String, password: String) {
        _isLoading.value = true
        try {
            viewModelScope.launch {
                authRepository.login(email, password)
            }
        } catch (e: Exception) {
            _message.value = e.localizedMessage
            Log.e(TAG, "onFailure: ${e.localizedMessage}")
        } finally {
            _isLoading.value = true
        }
    }
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
