package com.hasnarof.storyapp.data

import com.hasnarof.storyapp.data.remote.response.ErrorResponse

sealed class Resource<T>(val data: T?, val message: String?,
                         val errorResponse: ErrorResponse?) {
    class Success<T>(data: T?) : Resource<T>(data, null,null)
    class Error<T>(message: String) : Resource<T>(null, message,null)

    class ResponseError<T>(errorResponse: ErrorResponse?) : Resource<T>(null, null,errorResponse)
}