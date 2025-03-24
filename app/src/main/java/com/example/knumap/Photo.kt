package com.example.knumap

import android.net.Uri

data class Photo(
    val uri: Uri,
    val username: String,
    val timestamp: Long,
    var likes: Int,
    var isLiked: Boolean = false
)
