package com.novometgroup.auth

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @POST("api/sanctum/token")
    fun getToken(@Body registrationBody: RegistrationBody): Call<String>

//
//    @POST("login")
//    fun authenticate(@Body registrationBody: RegistrationBody,
//                     @Header("Cookie") cookie: String,
//                     @Header("X-Xsrf-Token") token: String): Call<String>
}