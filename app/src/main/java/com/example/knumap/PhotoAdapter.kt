package com.example.knumap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PhotoAdapter(private val photoList: MutableList<Photo>) :
    RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val photoImageView: ImageView = view.findViewById(R.id.photoImageView)
        private val usernameTextView: TextView = view.findViewById(R.id.usernameTextView)
        private val heartImageView: ImageView = view.findViewById(R.id.heartImageView)
        private val likeCountTextView: TextView = view.findViewById(R.id.likeCountTextView)

        fun bind(photo: Photo) {
            photoImageView.setImageURI(photo.uri)
            usernameTextView.text = photo.username
            likeCountTextView.text = photo.likes.toString()


            // 하트 이미지 설정 (상태에 따라)
            heartImageView.setImageResource(
                if (photo.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_border
            )

            // 하트 클릭 시
            heartImageView.setOnClickListener {
                photo.isLiked = !photo.isLiked
                if (photo.isLiked) {
                    photo.likes += 1
                } else {
                    photo.likes -= 1
                }
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
        photoList.add(0, photo)
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

