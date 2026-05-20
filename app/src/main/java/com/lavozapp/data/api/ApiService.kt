package com.lavozapp.data.api

import com.lavozapp.data.model.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body body: Map<String, String>): AuthLoginResponse

    @POST("auth/verify")
    suspend fun verify(@Body body: Map<String, String>): AuthVerifyResponse

    @GET("perfil")
    suspend fun getPerfil(@Header("Authorization") token: String): PerfilResponse

    companion object {
        private const val BASE_URL = "https://socios.lavozdepucon.cl/api/"

        fun create(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
