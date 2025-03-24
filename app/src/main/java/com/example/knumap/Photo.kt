package com.example.knumap

import android.net.Uri

data class Photo(
    val uri: Uri,           // 사진 URI
    val username: String,   // 사용자 이름
    val likes: Int,         // 좋아요 수
    val timestamp: Long
)
