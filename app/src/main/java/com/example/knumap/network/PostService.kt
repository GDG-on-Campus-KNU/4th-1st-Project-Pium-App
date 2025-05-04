package com.example.knumap.network

import com.example.knumap.model.BaseResponse
import com.example.knumap.model.GlobalResponse
import com.example.knumap.model.PostInfoResponse
import com.example.knumap.model.PostPagingResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PostService {

    @Multipart
    @POST("/api/post")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Part images: List<MultipartBody.Part>,       // ✅ 이미지 리스트는 그대로
        @Part("request") request: RequestBody          // ✅ 나머지 모든 필드는 JSON으로 묶어서 하나로 전송
    ): Response<GlobalResponse>



    @GET("/api/post?page=0&size=10")
    suspend fun getPostList(
        @Header("Authorization") token: String
    ): Response<BaseResponse<PostPagingResponse>>

    interface PostApiService {
        @GET("/api/post/{postId}")
        suspend fun getPostById(
            @Path("postId") postId: Long,
            @Header("Authorization") token: String
        ): Response<BaseResponse<PostInfoResponse>>
    }

}