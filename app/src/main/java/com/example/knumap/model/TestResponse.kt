package com.example.knumap.model

data class TestResponse(
    val success: Boolean,
    val data: MessageData?,
    val error: Any?
)