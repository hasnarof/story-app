package com.hasnarof.storyapp.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.*
import com.hasnarof.storyapp.data.database.StoryDatabase
import com.hasnarof.storyapp.data.local.StoryRemoteMediator
import com.hasnarof.storyapp.data.remote.retrofit.ApiService
import com.hasnarof.storyapp.domain.model.Story

class StoryRepository(private val storyDatabase: StoryDatabase, private val apiService: ApiService) {
    fun getStory(token: String): LiveData<PagingData<Story>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                initialLoadSize = 5,
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService, token),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData
    }

    companion object {
        private const val TAG = "StoryRepository"

        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            storyDatabase: StoryDatabase,
            apiService: ApiService
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(storyDatabase, apiService)
            }.also { instance = it }
    }
}