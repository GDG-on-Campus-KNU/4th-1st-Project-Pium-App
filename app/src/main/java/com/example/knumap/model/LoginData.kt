package com.example.knumap.model

import com.google.gson.annotations.SerializedName

data class LoginData(
    @SerializedName("account_id")
    val accountId: Long,
    @SerializedName("user_role")
    val userRole: String,
    @SerializedName("access_token")
    val jwtAccessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String
)