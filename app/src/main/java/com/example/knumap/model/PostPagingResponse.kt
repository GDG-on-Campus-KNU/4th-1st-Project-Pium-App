package com.example.knumap.model

data class PostPagingResponse(
    val content: List<PostInfo>,
    val totalElements: Int,
    val totalPages: Int,
    val size: Int,
    val page: Int
)
