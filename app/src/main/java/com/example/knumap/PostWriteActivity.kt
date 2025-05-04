package com.example.knumap

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.knumap.model.Post
import com.example.knumap.model.PostCreateRequest
import com.example.knumap.network.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class PostWriteActivity : AppCompatActivity() {

    private lateinit var imageUri: Uri
    private var latitude: Double = 0.0          // 🔥 GPS 위도
    private var longitude: Double = 0.0         // 🔥 GPS 경도



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_write)
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val userId = prefs.getLong("account_id", -1L).toString()
        val imageUriString = intent.getStringExtra("imageUri")
        if (imageUriString == null) {
            Log.e("DEBUG_POST", "imageUriString이 null입니다")
            finish()
            return
        }
        imageUri = Uri.parse(imageUriString)  // ✅ 문자열을 Uri 객체로 복원
        // ✅ 여기에 넣으면 좋음 (👉 여기!)
        val receivedLocationName = intent.getStringExtra("locationName") ?: "알 수 없음"
        val receivedLocationDetail = intent.getStringExtra("locationDetail") ?: "알 수 없음"

        findViewById<TextView>(R.id.locationTitle).text = receivedLocationName
        findViewById<TextView>(R.id.locationDetail).text = receivedLocationDetail
        // 🔥 좌표 정보도 함께 받기
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)

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


            // 🔥 서버에 업로드 요청
            uploadPost(description, locationName, locationDetail, hashtags, userId)

            /*
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
            */
        }
    }

    private fun uploadPost(description: String, locationName: String, locationDetail: String, hashtags: List<String>,userId: String  ) {

        lifecycleScope.launch {
            try {
                val accessToken = getAccessToken()                         // 🔥 SharedPreferences에서 JWT 토큰 가져오기
                val token = "Bearer $accessToken"

                // ✅ 이미지 Multipart 생성 (변경 없음)
                val imagePart = prepareImagePart("images", imageUri)

                // ✅ 🔽 여기가 수정 핵심 부분 🔽
                // 기존 title, content, latitude, longitude를 JSON으로 하나로 묶어 전송해야 함
                val postCreateRequest = PostCreateRequest(
                    title = locationName,
                    content = description,
                    latitude = latitude,
                    longitude = longitude
                )

                val gson = Gson()  // Gson import 필요
                val json = """
                    {
                      "title": "$locationName",
                      "content": "$description",
                      "latitude": $latitude,
                      "longitude": $longitude
                    }
                    """.trimIndent()

                Log.d("DEBUG_POST", "JSON requestBody content: $json")

                val requestBody = json.toRequestBody("application/json".toMediaType())
                // ✅ 서버 요청 (이제 JSON 하나만 전달)
                val response = RetrofitClient.postService.createPost(
                    token,
                    listOf(imagePart),
                    requestBody  // 🔥 여기서 JSON 묶음 전송
                )

                if (response.isSuccessful) {
                    Toast.makeText(this@PostWriteActivity, "게시글 업로드 성공!", Toast.LENGTH_SHORT).show()
                    // 만약 message 필드가 있다면 이렇게 출력 가능
                    Log.d("DEBUG_POST", "서버 응답 메시지: ${response.body()?.message}")
                    /*
                    val post = Post(
                        postId = 1,
                        imageUri = imageUri,
                        username = userId,
                        locationName = locationName,
                        locationDetail = locationDetail,
                        description = description,
                        hashtags = hashtags,
                        timestamp = System.currentTimeMillis(),
                        likes = 0,
                        isLiked = false
                    )

                    val resultIntent = Intent().apply {
                        putExtra("newPost", post)
                    }



                    setResult(RESULT_OK, resultIntent)
                    */
                    setResult(RESULT_OK)
                    finish()

                } else {
                    Log.e("DEBUG_POST", "업로드 실패: ${response.code()} ${response.errorBody()?.string()}")
                    Toast.makeText(this@PostWriteActivity, "업로드 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("DEBUG_POST", "예외 발생: ${e.message}", e)
                Toast.makeText(this@PostWriteActivity, "에러 발생", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ 이미지 압축하여 MultipartBody.Part로 변환
    private fun prepareImagePart(partName: String, uri: Uri): MultipartBody.Part {
        val inputStream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        if (bitmap == null) {
            Log.e("DEBUG_IMAGE", "❌ 이미지 디코딩 실패 - bitmap == null")
        } else {
            Log.d("DEBUG_IMAGE", "✅ 이미지 디코딩 성공")
        }
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream) // 압축률: 60%
        val byteArray = stream.toByteArray()

        Log.d("DEBUG_IMAGE", "압축된 이미지 크기: ${byteArray.size} bytes")

        val requestBody = byteArray.toRequestBody("image/jpeg".toMediaType())
        return MultipartBody.Part.createFormData(partName, "image.jpg", requestBody)
    }


    // 🔥 AccessToken 가져오기
    private fun getAccessToken(): String {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        return prefs.getString("access_token", "") ?: ""
    }


    private fun extractHashtags(text: String): List<String> {
        return Regex("#(\\w+)").findAll(text).map { it.value }.toList()
    }
}
