package com.novometgroup.auth

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @GET("sanctum/csrf-cookie")
    fun getCookies(): Call<MyResponse>

    @POST("login")
    fun authenticate(@Body registrationBody: RegistrationBody,
                     @Header("Cookie") cookie: String,
                     @Header("X-Xsrf-Token") token: String): Call<String>
}