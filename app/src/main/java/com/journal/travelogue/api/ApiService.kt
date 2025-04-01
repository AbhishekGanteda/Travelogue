package com.journal.travelogue.api

import com.journal.travelogue.models.Like
import com.journal.travelogue.models.LoginResponse
import com.journal.travelogue.models.Post
import com.journal.travelogue.models.Save
import com.journal.travelogue.models.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("api/users/signup")
    fun registerUser(@Body user: User): Call<User>

    @POST("api/users/login")
    fun loginUser(@Body credentials: User): Call<LoginResponse>

    @Multipart
    @PUT("api/users/update/{id}")
    fun updateUserProfile(
        @Path("id") userId: Int,
        @Part("name") name: RequestBody?,
        @Part("email") email: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Call<User>

    @Multipart
    @POST("api/posts")
    fun postTravelItem(
        @Part("user_id") userId: Int,
        @Part("place_name") placeName: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Call<Post>

    @GET("api/posts/all/{userId}")
    fun getAllPosts(
        @Path("userId") userId: Int?
    ) : Call<List<Post>>

    @GET("api/users/{id}")
    fun getUserById(
        @Path("id") id : Int?,
    ): Call<User>

    @GET("api/likes/liked/{postId}")
    fun postLikedOrNot(
        @Path("postId") postId: Int?,
        @Query("userId") userId: Int?
    ): Call<Map<String, Boolean>>

    @GET("api/saved/saved/{postId}")
    fun postSavedOrNot(
        @Path("postId") postId: Int?,
        @Query("userId") userId: Int?
    ): Call<Map<String, Boolean>>

    @GET("api/likes/{postId}")
    fun getLikesCount(
        @Path("postId") postId: Int?,
    ): Call<Int>

    @GET("api/saved/{postId}")
    fun getSavedCount(
        @Path("postId") postId: Int?,
    ): Call<Int>

    @POST("api/likes")
    fun addToLikeTable(
        @Body details:Map<String,Int?>
    ): Call<Like>

    @DELETE("api/likes/{userId}/{postId}")
    fun removeFromLikeTable(
        @Path("userId") userId: Int?,
        @Path("postId") postId: Int?
    ): Call<String>

    @POST("api/saved")
    fun addToSavedTable(
        @Body details:Map<String,Int?>
    ): Call<Save>

    @DELETE("api/saved/{userId}/{postId}")
    fun removeFromSavedTable(
        @Path("userId") userId: Int?,
        @Path("postId") postId: Int?
    ): Call<String>
}
