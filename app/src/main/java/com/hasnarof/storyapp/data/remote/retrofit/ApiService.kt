package com.hasnarof.storyapp.data.remote.retrofit

import com.hasnarof.storyapp.data.remote.response.LoginResponse
import com.hasnarof.storyapp.data.remote.response.RegisterResponse
import com.hasnarof.storyapp.data.remote.response.StoriesResponse
import com.hasnarof.storyapp.data.remote.response.StoryAddResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @GET("stories")
    fun stories(
        @Header("Authorization") token: String,
    ): Call<StoriesResponse>

    @Multipart
    @POST("stories")
    fun addStory(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
    ): Call<StoryAddResponse>

}