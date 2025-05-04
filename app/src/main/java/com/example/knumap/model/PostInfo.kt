package com.example.knumap.model

data class PostInfo(
    val postId: Long,
    val title: String,
    val content: String,
    val nickname: String,
    val latitude: Double,
    val longitude: Double,
    val like: Int,
    val images: List<String>  // 🔥 여기 중요
)