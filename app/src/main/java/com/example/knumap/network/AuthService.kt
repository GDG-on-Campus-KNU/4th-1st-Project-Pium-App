package com.example.knumap.network

import com.example.knumap.model.AccessTokenRequest
import com.example.knumap.model.KakaoLoginRequest
import com.example.knumap.model.KakaoLoginResponse
import com.example.knumap.model.KakaoLoginTokenResponse
import com.example.knumap.model.KakaoLoginUrlResponseDto
import com.example.knumap.model.TestResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthService {
    @GET("/api/v1/oauth/kakao")
    suspend fun getKakaoLoginUrl(): Response<KakaoLoginUrlResponseDto>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/oauth/kakao/login")
    suspend fun kakaoLogin(@Body request: KakaoLoginRequest): Response<KakaoLoginResponse>

    @GET("/api/v1/test")
    suspend fun getTestMessage(
        @Header("Authorization") token: String
    ): Response<TestResponse>

}