package com.dicoding.picodiploma.loginwithanimation.api

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @field:SerializedName("message")
    val message: String
)
