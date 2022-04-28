package com.hasnarof.storyapp.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.*
import com.hasnarof.storyapp.data.Resource
import com.hasnarof.storyapp.data.database.StoryDatabase
import com.hasnarof.storyapp.data.local.StoryRemoteMediator
import com.hasnarof.storyapp.data.remote.response.StoriesResponse
import com.hasnarof.storyapp.data.remote.retrofit.ApiService
import com.hasnarof.storyapp.domain.model.Story
import com.hasnarof.storyapp.helper.getErrorResponse

class StoryRepository(private val storyDatabase: StoryDatabase, private val apiService: ApiService) {
    fun getStory(token: String): LiveData<PagingData<Story>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                initialLoadSize = 5,
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService, "Bearer $token"),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData
    }

    suspend fun getStoriesWithLocation(token: String): Resource<StoriesResponse> {
        try {
            val response = apiService.getStoriesWithLocation(token = "Bearer $token", location = 1)
            if(response.isSuccessful) {
                val result = response.body()
                return Resource.Success(result)
            } else {
                return Resource.ResponseError(getErrorResponse(response.errorBody()!!.string()))
            }
        } catch (e: Exception) {
            Log.d(TAG, "${e.message}")
            return Resource.Error(e.message ?: "An error occured")
        }
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