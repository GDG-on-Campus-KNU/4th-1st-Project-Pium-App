package com.example.knumap.model

import android.net.Uri

data class Post(
    val imageUri: Uri,
    val locationName: String,
    val locationDetail: String,
    val description: String,
    val hashtags: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)