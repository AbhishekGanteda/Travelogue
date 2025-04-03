package com.journal.travelogue.api

import android.content.Context
import com.journal.travelogue.AuthInterceptor
import com.journal.travelogue.MyApplication
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    const val ip = "10.123.13.138"
    private const val BASE_URL = "http://$ip:5000"

    val instance: ApiService by lazy {
        val context = MyApplication.instance.applicationContext
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(provideOkHttpClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }

    private fun provideOkHttpClient(context: Context): OkHttpClient {
        val tokenProvider = {
            val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val token = sharedPref.getString("TOKEN", null)
            if (token == null) {
                sharedPref.edit().remove("TOKEN").apply()
            }
            token
        }
        val authInterceptor = AuthInterceptor(tokenProvider)
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }
}
