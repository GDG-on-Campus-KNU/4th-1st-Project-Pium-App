package com.example.knumap.model

data class BaseResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: Any? // 필요 없다면 생략 가능
)
