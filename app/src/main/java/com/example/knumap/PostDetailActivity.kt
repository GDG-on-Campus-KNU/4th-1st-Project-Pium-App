package com.example.knumap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.knumap.model.Post
import com.example.knumap.model.PostInfoResponse
import com.example.knumap.network.RetrofitClient.postApiService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.net.Uri


class PostDetailActivity : AppCompatActivity() {
    private lateinit var likeIcon: ImageView
    private lateinit var likeCount: TextView
    private lateinit var post: Post
    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        val postId = intent.getLongExtra("postId", -1)
        Log.d("DEBUG_DETAIL", "수신된 postId: $postId")
        if (postId == -1L) {
            Log.e("DEBUG_DETAIL", "🚨 postId가 전달되지 않았습니다.")
            finish()
            return
        }
        fetchPostDetailFromServer(postId)
        /*
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

         */
    }
    */
    //신규
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)
        Log.d("DEBUG_DETAIL", "🔥 PostDetailActivity 시작됨")

        val postId = intent.getLongExtra("postId", -1)
        Log.d("DEBUG_DETAIL", "수신된 postId: $postId")
        if (postId == -1L) {
            Log.e("DEBUG_DETAIL", "🚨 postId가 전달되지 않았습니다.")
            finish()
            return
        }

        fetchPostDetailFromServer(postId)
    }
    /*
    private fun fetchPostDetailFromServer(postId: Long) {
        lifecycleScope.launch {
            try {
                val token = getAccessToken()
                val response = postApiService.getPostById(postId, "Bearer $token")

                if (response.isSuccessful) {
                    Log.d("DEBUG_API", "게시글 상세 조회 성공 - postId: $postId")
                    response.body()?.let { postInfo ->
                        renderPost(postInfo,postId)
                    } ?: Log.e("PostDetail", "응답 본문이 null입니다.")
                } else {
                    Log.e("DEBUG_API", "응답 실패: ${response.code()}")
                    Log.e("PostDetail", "응답 실패: ${response.code()} ${response.errorBody()?.string()}")
                    finish()
                }

            } catch (e: Exception) {
                Log.e("PostDetail", "서버 요청 실패", e)
                finish()
            }
        }
    }
    */

    /*
    private fun renderPost(info: PostInfoResponse, postid: Long) {
        // 🔁 Post 객체 생성 (기존 Post 클래스가 있으면 맞춰서 변환)
        val newPost = Post(
            postId = postid,  // ✅ 필요하면 postId도 추가
            imageUri = Uri.parse(info.images.firstOrNull() ?: ""),  // ✅ null-safe
            username = info.nickname,
            locationName = "알 수 없음",  // ✅ 위치명 변환 로직 필요 (역지오코딩 또는 주소 API)
            locationDetail = "",
            description = info.content,
            hashtags = emptyList(),  // ✅ 서버에서 제공 시 반영
            timestamp = System.currentTimeMillis(),
            likes = info.like,
            isLiked = false  // ✅ 좋아요 여부도 서버에서 내려주면 반영
        )
        this.post = newPost  // ✅ 오류 해결
        updateLikeUI(post)

        // ✅ UI에 반영
        findViewById<ImageView>(R.id.detailImageView).let {
            Glide.with(this).load(post.imageUri).into(it)
        }
        findViewById<TextView>(R.id.usernameText).text = post.username
        findViewById<TextView>(R.id.locationTitle).text = "\uD83D\uDCCD ${post.locationName}"
        findViewById<TextView>(R.id.locationDetail).text = post.locationDetail
        findViewById<TextView>(R.id.postContent).text = post.description

        val formattedTime = getTimeAgo(post.timestamp)
        findViewById<TextView>(R.id.timeText).text = formattedTime

        val formattedDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date(post.timestamp))
        findViewById<TextView>(R.id.dateText).text = "\uD83D\uDCC5 $formattedDate"

        likeIcon = findViewById(R.id.likeIcon)
        likeCount = findViewById(R.id.likeCount)
        updateLikeUI(post)

        // ✅ 좋아요 토글
        likeIcon.setOnClickListener {
            post.isLiked = !post.isLiked
            post.likes += if (post.isLiked) 1 else -1
            updateLikeUI(post)
        }

        // ✅ 뒤로가기 버튼 설정
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("updatedPost", post)  // 🔁 서버 연동할 거면 안 보내도 됨
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
    */
    private fun fetchPostDetailFromServer(postId: Long) {
        lifecycleScope.launch {
            try {
                val token = getAccessToken()
                Log.d("DEBUG_API", "🔑 access_token 가져오기 성공: $token")
                val response = postApiService.getPostById(postId, "Bearer $token")

                if (response.isSuccessful) {
                    val baseResponse = response.body()
                    Log.d("DEBUG_API", "✅ 게시글 상세 조회 성공 - postId: $postId")
                    Log.d("DEBUG_API", "📦 BaseResponse 전체: $baseResponse")

                    baseResponse?.data?.let { postInfo ->
                        Log.d("DEBUG_RENDER", "📦 원시 PostInfoResponse: $postInfo")
                        renderPost(postInfo, postId)
                    } ?: Log.e("DEBUG_API", "❌ 응답 본문 내 data가 null입니다.")
                } else {
                    Log.e("DEBUG_API", "❌ 응답 실패: ${response.code()}")
                    Log.e("DEBUG_API", "❌ 에러 내용: ${response.errorBody()?.string()}")
                    finish()
                }

            } catch (e: Exception) {
                Log.e("DEBUG_API", "🚨 서버 요청 실패", e)
                finish()
            }
        }
    }
    private fun renderPost(info: PostInfoResponse, postid: Long) {
        Log.d("DEBUG_RENDER", "🔧 renderPost 호출됨 - postId: $postid")

        // 🚨 원시 데이터 전체 로그 찍기 (디버깅용)
        Log.d("DEBUG_RENDER", "📦 원시 PostInfoResponse: $info")

        // ✅ 먼저 likeIcon, likeCount 초기화부터 하세요 (아래 updateLikeUI 호출 전에 반드시)
        likeIcon = findViewById(R.id.likeIcon)
        likeCount = findViewById(R.id.likeCount)

        // ✅ images 자체가 null이 아닐 경우만 처리 (서버 응답에 따라 List<String>? 일 수 있으므로)
        val imageUrl = info.images.firstOrNull()  // ← 여기서 오류 발생 가능 (images가 null이 아니라고 확신할 수 없을 경우)
        if (imageUrl.isNullOrEmpty()) {
            Log.w("DEBUG_RENDER", "🚫 이미지가 없습니다. Glide 로드 생략됨.")
        } else {
            Log.d("DEBUG_RENDER", "📷 Glide 이미지 로드 시작: $imageUrl")
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(findViewById(R.id.detailImageView))
        }

        // ✅ Post 생성 시 모든 필드를 null-safe하게!
        val newPost = Post(
            postId = postid,
            imageUri = if (imageUrl.isNullOrEmpty()) Uri.EMPTY else Uri.parse(imageUrl),
            username = info.nickname ?: "알 수 없음",         // ← null 방지
            locationName = info.title ?: "알 수 없음",        // ← 서버 title을 locationName으로 활용
            locationDetail = "",
            description = info.content ?: "",
            hashtags = emptyList(),
            timestamp = System.currentTimeMillis(),
            likes = info.like ?: 0,                          // ← 서버가 null일 수 있으므로 ?: 0
            isLiked = false
        )
        Log.d("DEBUG_RENDER", "🧩 Post 객체 생성 완료: $newPost")
        this.post = newPost

        // ✅ UI에 반영
        findViewById<TextView>(R.id.usernameText).text = post.username
        findViewById<TextView>(R.id.locationTitle).text = "\uD83D\uDCCD ${post.locationName}"
        findViewById<TextView>(R.id.locationDetail).text = post.locationDetail
        findViewById<TextView>(R.id.postContent).text = post.description

        val formattedTime = getTimeAgo(post.timestamp)
        findViewById<TextView>(R.id.timeText).text = formattedTime

        val formattedDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date(post.timestamp))
        findViewById<TextView>(R.id.dateText).text = "\uD83D\uDCC5 $formattedDate"

        // ✅ 좋아요 수 UI 갱신
        updateLikeUI(post)

        // ✅ 좋아요 토글
        likeIcon.setOnClickListener {
            post.isLiked = !post.isLiked
            post.likes += if (post.isLiked) 1 else -1
            updateLikeUI(post)
        }

        // ✅ 뒤로가기 버튼 설정
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("updatedPost", post)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }


    private fun getAccessToken(): String {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        return prefs.getString("access_token", "") ?: ""
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
