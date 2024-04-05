package com.novometgroup.auth

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthApi {

    @GET("sanctum/csrf-cookie")
    fun getCookies(): Call<MyResponse>

    @Multipart
    @POST("login")
    fun authenticate(@Part("email") email: String,
                     @Part("password") password: String,
                     @Header("Cookie") cookie: String,
                     @Header("X-Xsrf-Token") token: String): Call<MyResponse>
}