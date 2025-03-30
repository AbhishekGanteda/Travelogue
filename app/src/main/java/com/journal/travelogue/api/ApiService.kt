package com.journal.travelogue.api

import com.journal.travelogue.models.LoginResponse
import com.journal.travelogue.models.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/users/signup")
    fun registerUser(@Body user: User): Call<User>

    @POST("api/users/login")
    fun loginUser(@Body credentials: User): Call<LoginResponse>
}