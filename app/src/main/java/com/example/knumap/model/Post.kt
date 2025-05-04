package com.example.knumap.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post(
    val postId: Long,
    val imageUri: Uri,
    val username: String,
    val locationName: String,
    val locationDetail: String,
    val description: String,
    val hashtags: List<String>,
    val timestamp: Long = System.currentTimeMillis(),
    var likes: Int = 0,
    var isLiked: Boolean = false

) : Parcelable