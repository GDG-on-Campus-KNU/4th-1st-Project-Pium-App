package com.example.knumap

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PhotoAdapter(private val photoList: MutableList<Photo>) :
    RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val photoImageView: ImageView = view.findViewById(R.id.photoImageView)
        private val usernameTextView: TextView = view.findViewById(R.id.usernameTextView)
        private val likeButton: ImageButton = view.findViewById(R.id.likeButton)
        private val likeCountTextView: TextView = view.findViewById(R.id.likeCountTextView)

        fun bind(photo: Photo) {
            photoImageView.setImageURI(photo.uri)
            usernameTextView.text = photo.username
            likeCountTextView.text = photo.likes.toString()

            // 좋아요 버튼 클릭 시 증가
            likeButton.setOnClickListener {
                val newLikes = photo.likes + 1
                photoList[adapterPosition] = photo.copy(likes = newLikes)
                notifyItemChanged(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photoList[position])
    }

    override fun getItemCount(): Int = photoList.size

    fun addPhoto(photo: Photo) {
        photoList.add(0, photo) // 최신 사진이 맨 위에 오도록
        notifyItemInserted(0)
    }

    fun sortByLatest() {
        photoList.sortByDescending { it.timestamp }
        notifyDataSetChanged()
    }

    fun sortByPopular() {
        photoList.sortByDescending { it.likes }
        notifyDataSetChanged()
    }

}

