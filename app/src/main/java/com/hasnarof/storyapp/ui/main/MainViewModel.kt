package com.hasnarof.storyapp.ui.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.hasnarof.storyapp.Injection
import com.hasnarof.storyapp.data.preferences.AuthPreferences
import com.hasnarof.storyapp.data.remote.response.LoginResult
import com.hasnarof.storyapp.data.repository.AuthRepository
import com.hasnarof.storyapp.ui.home.HomeViewModel

class MainViewModel(private val authRepository: AuthRepository) : ViewModel() {
    fun getCurrentUser(): LiveData<LoginResult> {
        return authRepository.getCurrentUser().asLiveData()
    }
}

class MainViewModelFactory (private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                Injection.provideAuthRepository(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}