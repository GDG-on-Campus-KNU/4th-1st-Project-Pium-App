package com.example.knumap.model

data class KakaoLoginTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val memberId: Long,
    val nickname: String
)