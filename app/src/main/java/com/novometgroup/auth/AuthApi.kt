package com.novometgroup.auth

import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthApi {
    @Multipart
    @POST("path/")
    fun authenticate(@Part("login") login: String, @Part("password") password: String): Call<AuthResponse>
}