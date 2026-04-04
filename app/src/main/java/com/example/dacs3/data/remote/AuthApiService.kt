package com.example.dacs3.data.remote

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApiService {
    @FormUrlEncoded
    @POST("api/auth/send-otp")
    suspend fun sendOtp(
        @Field("email") email: String
    ): Response<AuthResponse>

    @FormUrlEncoded
    @POST("api/auth/reset-password-with-otp")
    suspend fun resetPasswordWithOtp(
        @Field("email") email: String,
        @Field("otp") otp: String,
        @Field("newPassword") newPassword: String
    ): Response<AuthResponse>
}

data class AuthResponse(
    val success: Boolean,
    val message: String
)
