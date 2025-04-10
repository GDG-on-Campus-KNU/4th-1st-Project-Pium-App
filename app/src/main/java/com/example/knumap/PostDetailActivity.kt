package com.example.knumap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.knumap.model.Post
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class PostDetailActivity : AppCompatActivity() {
    private lateinit var likeIcon: ImageView
    private lateinit var likeCount: TextView
    private lateinit var post: Post

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        post = intent.getParcelableExtra<Post>("newPost") ?: return

        findViewById<ImageView>(R.id.detailImageView).setImageURI(post.imageUri)
        findViewById<TextView>(R.id.usernameText).text = post.username
        findViewById<TextView>(R.id.locationTitle).text = "\uD83D\uDCCD ${post.locationName}"
        findViewById<TextView>(R.id.locationDetail).text = post.locationDetail
        findViewById<TextView>(R.id.postContent).text = post.description

        findViewById<TextView>(R.id.likeCount).text = post.likes.toString()

        // 🔹 시간 포맷팅
        val formattedTime = getTimeAgo(post.timestamp)
        findViewById<TextView>(R.id.timeText).text = formattedTime

        // 🔹 날짜 텍스트
        val formattedDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date(post.timestamp))
        findViewById<TextView>(R.id.dateText).text = "\uD83D\uDCC5 $formattedDate"

        // 🔹 좋아요 토글
        likeIcon = findViewById(R.id.likeIcon)
        likeCount = findViewById(R.id.likeCount)
        updateLikeUI(post)
        likeIcon.setOnClickListener {
            Log.d("DEBUG_POST", "좋아요 아이콘 클릭됨. 현재 상태: ${post.isLiked}, 좋아요 수: ${post.likes}")
            try {
                post.isLiked = !post.isLiked
                post.likes += if (post.isLiked) 1 else -1
                updateLikeUI(post)


                Log.d("DEBUG_POST", "좋아요 상태 업데이트 완료. 현재 상태: ${post.isLiked}, 좋아요 수: ${post.likes}")
            } catch (e: Exception) {
                Log.e("DEBUG_POST", "좋아요 처리 중 오류 발생", e)
            }
        }

        // 🔹 뒤로가기 버튼
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("updatedPost", post)
            }
            Log.d("DEBUG_POST", "뒤로가기: setResult 호출됨! 좋아요 수: ${post.likes}, isLiked: ${post.isLiked}")
            setResult(RESULT_OK, resultIntent)  // 🔥 뒤로가기 누를 때만 상태 전달
            finish()

        }
    }
    override fun onBackPressed() {
        val resultIntent = Intent().apply {
            putExtra("updatedPost", post)
        }
        setResult(RESULT_OK, resultIntent)
        super.onBackPressed()
    }

    private fun getTimeAgo(timeMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timeMillis
        val minutes = diff / (60 * 1000)
        val hours = diff / (60 * 60 * 1000)
        val days = diff / (24 * 60 * 60 * 1000)

        return when {
            minutes < 1 -> "방금 전"
            minutes < 60 -> "${minutes}분 전"
            hours < 24 -> "${hours}시간 전"
            else -> "${days}일 전"
        }
    }

    fun updateLikeUI(post: Post) {
        Log.d("DEBUG_POST", "UI 업데이트: isLiked = ${post.isLiked}, 좋아요 수 = ${post.likes}")
        likeIcon.setImageResource(
            if (post.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_border
        )
        likeCount = findViewById(R.id.likeCount)
        likeCount.text = post.likes.toString()

    }


}
