package com.example.knumap.model

import com.google.gson.annotations.SerializedName

data class KakaoLoginRequest(
    @SerializedName("access_token")
    val accessToken: String
)