package com.example.knumap.model

data class PostCreateRequest(
    val title: String,
    val content: String,
    val latitude: Double,
    val longitude: Double
)
