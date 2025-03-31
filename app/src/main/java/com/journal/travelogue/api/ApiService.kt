package com.journal.travelogue.api

import com.journal.travelogue.models.LoginResponse
import com.journal.travelogue.models.Post
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

}
