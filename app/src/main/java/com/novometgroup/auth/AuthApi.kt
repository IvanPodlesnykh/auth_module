package com.novometgroup.auth

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("api/sanctum/token")
    fun getToken(@Body registrationBody: RegistrationBody): Call<String>

    @POST("api/sanctum/token_revoke")
    fun deleteToken(@Header("Authorization") bearer: String): Call<Int>
}