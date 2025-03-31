package com.journal.travelogue.api

import com.journal.travelogue.models.LoginResponse
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
        @Part image: MultipartBody.Part? // Make image upload optional
    ): Call<User>
}
