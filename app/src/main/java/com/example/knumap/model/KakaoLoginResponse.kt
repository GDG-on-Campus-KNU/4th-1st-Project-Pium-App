package com.example.knumap.model

data class KakaoLoginResponse(
    val success: Boolean,
    val data: LoginData?,
    val error: Any?
)
