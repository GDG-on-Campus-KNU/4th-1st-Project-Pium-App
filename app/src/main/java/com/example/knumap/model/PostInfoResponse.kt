package com.example.knumap.model

data class PostInfoResponse(
    val title: String,
    val content: String,
    val nickname: String,
    val latitude: Double,
    val longitude: Double,
    val like: Int,
    val images: List<String>
)
