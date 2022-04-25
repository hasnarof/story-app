package com.hasnarof.storyapp.ui.story.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hasnarof.storyapp.data.preferences.AuthPreferences

class StoryAddViewModelFactory(private val pref: AuthPreferences) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoryAddViewModel::class.java)) {
            return StoryAddViewModel(pref) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}