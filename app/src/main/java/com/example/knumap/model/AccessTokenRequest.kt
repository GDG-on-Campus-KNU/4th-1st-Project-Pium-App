package com.example.knumap.model

import com.google.gson.annotations.SerializedName

data class AccessTokenRequest(
    @SerializedName("access_token")
    val accessToken: String
)
