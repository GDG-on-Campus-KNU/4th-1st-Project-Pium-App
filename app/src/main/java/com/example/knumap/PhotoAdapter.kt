package com.example.knumap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.knumap.model.Post

class PhotoAdapter(
    private val photoList: MutableList<Photo>,
    private val onItemClick: (Post) -> Unit
    ) :
    RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val photoImageView: ImageView = view.findViewById(R.id.photoImageView)
        private val usernameTextView: TextView = view.findViewById(R.id.usernameTextView)
        private val heartImageView: ImageView = view.findViewById(R.id.heartImageView)
        private val likeCountTextView: TextView = view.findViewById(R.id.likeCountTextView)

        fun bind(photo: Photo) {
            val post = photo.post
            photoImageView.setImageURI(post.imageUri)
            usernameTextView.text = "익명"
            likeCountTextView.text = post.likes.toString()


            // 하트 이미지 설정 (상태에 따라)
            heartImageView.setImageResource(
                if (post.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_border
            )

            // 하트 클릭 시
            heartImageView.setOnClickListener {
                photo.isLiked = !photo.isLiked
                photo.likes += if (photo.isLiked) 1 else -1
                notifyItemChanged(adapterPosition)

            }
            // 🔥 [추가] 사진 클릭 시 콜백 실행
            photoImageView.setOnClickListener {
                onItemClick(post) // 🔥 클릭된 Photo 객체 전달
            }
        }
    }
    fun getPhotoList(): MutableList<Photo> {
        return photoList
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

    fun addPhoto(post: Post) {
        photoList.add(0, Photo(post)) // ✅ 내부에서 감싸서 Photo 객체로 변환
        notifyItemInserted(0)
    }

    fun sortByLatest() {
        photoList.sortByDescending { it.post.timestamp }
        notifyDataSetChanged()
    }

    fun sortByPopular() {
        photoList.sortByDescending { it.post.likes }
        notifyDataSetChanged()
    }
}

