package com.example.knumap

import android.net.Uri
import android.os.Parcelable
import com.example.knumap.model.Post
import kotlinx.parcelize.Parcelize

@Parcelize
data class Photo(
    val post: Post
) : Parcelable{
    val uri: Uri get() = post.imageUri
    val username: String get() = post.username
    val timestamp: Long get() = post.timestamp
    var likes: Int
        get() = post.likes
        set(value) { post.likes = value }
    var isLiked: Boolean
        get() = post.isLiked
        set(value) { post.isLiked = value }
}
