package com.example.knumap

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.knumap.model.Post

class PostWriteActivity : AppCompatActivity() {

    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_write)

        val imageUriString = intent.getStringExtra("imageUri")
        if (imageUriString == null) {
            Log.e("DEBUG_POST", "imageUriString이 null입니다")
            finish()
            return
        }
        imageUri = Uri.parse(imageUriString)  // ✅ 문자열을 Uri 객체로 복원
        Log.d("DEBUG_POST", "받은 imageUri: $imageUri")

        // 🔍 2. 사진 미리보기
        findViewById<ImageView>(R.id.imagePreview).apply {
            setImageURI(imageUri)
            Log.d("DEBUG_POST", "이미지 미리보기 설정 완료")
        }

        // 🔍 3. 공유 버튼 눌렀을 때 처리
        findViewById<ImageButton>(R.id.submitButton).setOnClickListener {
            Log.d("DEBUG_POST", "공유하기 버튼 클릭됨")

            val description = findViewById<EditText>(R.id.descriptionEdit).text.toString()
            val locationName = findViewById<TextView>(R.id.locationTitle).text.toString()
            val locationDetail = findViewById<TextView>(R.id.locationDetail).text.toString()
            val hashtags = extractHashtags(description)

            Log.d("DEBUG_POST", "내용: $description")
            Log.d("DEBUG_POST", "해시태그: $hashtags")

            val post = Post(
                imageUri = imageUri,
                username = "user_123",
                locationName = locationName,
                locationDetail = locationDetail,
                description = description,
                hashtags = hashtags,
                timestamp = System.currentTimeMillis(),
                likes = (1..1000).random(),  // ✅ 랜덤 좋아요 수
                isLiked = false              // ✅ 초기 상태는 좋아요 안 누른 상태
            )

            Log.d("DEBUG_POST", "Post 객체 생성 완료: $post")

            val resultIntent = Intent().apply {
                putExtra("newPost", post)
            }

            setResult(RESULT_OK, resultIntent)
            Log.d("DEBUG_POST", "setResult 호출 완료, 액티비티 종료")
            finish()
        }
    }

    private fun extractHashtags(text: String): List<String> {
        return Regex("#(\\w+)").findAll(text).map { it.value }.toList()
    }
}
