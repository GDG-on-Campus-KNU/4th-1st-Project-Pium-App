package com.example.knumap.network


import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
object RetrofitClient {
    private const val BASE_URL = "http://3.35.215.102:8080"
    // 🔥 Timeout 설정을 가진 OkHttpClient 정의
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)   // 연결 시도 제한
        .readTimeout(30, TimeUnit.SECONDS)      // 서버 응답 대기 시간
        .writeTimeout(30, TimeUnit.SECONDS)     // 요청 본문 전송 제한
        .build()
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authService: AuthService = retrofit.create(AuthService::class.java)

    // 🔥 여기에 추가
    val postService: PostService = retrofit.create(PostService::class.java)
    val postApiService = retrofit.create(PostService.PostApiService::class.java)

}