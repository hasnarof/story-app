package com.hasnarof.storyapp.data.remote.response

import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import com.hasnarof.storyapp.domain.model.Story

data class StoriesResponse(

	@field:SerializedName("listStory")
	val listStory: List<StoryItem>,

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class StoryItem(
	val photoUrl: String,
	val createdAt: String,
	val name: String,
	val description: String,
	val lon: Double? = null,
	val id: String,
	val lat: Double? = null
)